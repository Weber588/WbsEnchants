package wbs.enchants.enchantment.helper;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.util.EnchantUtils;

import java.util.*;
import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public interface EnchantingTableEnchant extends BlockStateEnchant<EnchantingTable> {

    int DEFAULT_MAX_POWER = 15;

    @Override
    default Class<EnchantingTable> getStateClass() {
        return EnchantingTable.class;
    }

    default int getEnchantmentCost(long seed, int slot, int power) {
        return getEnchantmentCost(seed, slot, power, DEFAULT_MAX_POWER);
    }
    default int getEnchantmentCost(long seed, int slot, int power, int maxPower) {
        RandomSource random = RandomSource.create(seed);

        if (power > maxPower) {
            power = maxPower;
        }

        int i = random.nextInt(8) + 1 + (power >> 1) + random.nextInt(power + 1);
        if (slot == 0) {
            return Math.max(i / 3, 1);
        } else {
            return slot == 1 ? i * 2 / 3 + 1 : Math.max(i, power * 2);
        }
    }

    default void updateEnchantmentOffers(PrepareItemEnchantEvent event) {
        updateEnchantmentOffers(event, event.getView().getEnchantmentSeed());
    }

    default void updateEnchantmentOffers(PrepareItemEnchantEvent event, long seed) {
        updateEnchantmentOffers(event, seed, 0);
    }

    default void updateEnchantmentOffers(PrepareItemEnchantEvent event, long seed, int salt) {
        @Nullable EnchantmentOffer[] offers = event.getOffers();
        int[] costs = new int[offers.length];

        for (int cost = 0; cost < offers.length; cost++) {
            EnchantmentOffer offer = offers[cost];
            if (offer != null) {
                costs[cost] = offer.getCost();
            }
        }

        updateEnchantmentOffers(event, seed, salt, costs);
    }
    default void updateEnchantmentOffers(PrepareItemEnchantEvent event, long seed, int salt, int[] costs) {
        Player player = event.getEnchanter();
        ItemStack item = event.getItem();

        seed += salt;

        Random random = new Random(seed);

        for (int slot = 0; slot < 3; slot++) {
            EnchantmentOffer offer = event.getOffers()[slot];
            if (offer != null) {
                Map<Enchantment, Integer> enchantments = getEnchantments(
                        player,
                        seed,
                        item,
                        slot,
                        costs[slot]
                );

                if (enchantments.isEmpty()) {
                    continue;
                }

                Enchantment[] sortedOptions = enchantments.keySet().stream()
                        .sorted(Comparator.comparing(enchant -> enchant.key().asString()))
                        .toArray(Enchantment[]::new);

                Enchantment show = sortedOptions[random.nextInt(sortedOptions.length)];

                offer.setEnchantment(show);
                offer.setEnchantmentLevel(enchantments.get(show));
            }
        }
    }

    default Map<Enchantment, Integer> getEnchantments(Player player, long seed, ItemStack stack, int salt, int cost) {
        return getEnchantments(seed, stack, salt, cost);
    }
    default Map<Enchantment, Integer> getEnchantments(long seed, ItemStack stack, int salt, int cost) {
        RandomSource source = RandomSource.create(seed + salt);

        Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Tag<@NotNull Enchantment> availableOnTable = registry.getTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE);
        if (availableOnTable.isEmpty()) {
            return Map.of();
        } else {
            Map<Enchantment, Integer> enchantments = selectEnchantment(
                    source,
                    stack,
                    cost,
                    availableOnTable.resolve(registry)
            );

            if (stack.getType() == Material.BOOK && enchantments.size() > 1) {
                Enchantment key = getRandomKey(enchantments, source);

                enchantments.remove(key);
            }

            return enchantments;
        }
    }

    private static <T extends Keyed> T getRandomKey(Map<T, Integer> map, RandomSource source) {
        List<T> list = map.keySet().stream().sorted(Comparator.comparing(Keyed::getKey)).toList();
        return list.get(source.nextInt(map.size()));
    }
    private static <T extends Keyed> Optional<T> getRandomKey(Map<T, Integer> map, RandomSource source, ToIntFunction<T> weightProvider) {
        List<T> list = map.keySet().stream().sorted(Comparator.comparing(Keyed::getKey)).toList();
        return WeightedRandom.getRandomItem(source, list, weightProvider);
    }
    private static <T extends Keyed> T getLast(Map<T, Integer> map) {
        List<T> list = map.keySet().stream().sorted(Comparator.comparing(Keyed::getKey)).toList();
        return list.getLast();
    }

    default Map<Enchantment, Integer> selectEnchantment(RandomSource random, ItemStack stack, final int level, Collection<Enchantment> availableOnTable) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        Enchantable enchantable = stack.getData(DataComponentTypes.ENCHANTABLE);
        if (enchantable == null) {
            return enchantments;
        } else {
            int modifiedLevel = getModifiedLevel(random, level, enchantable);
            Map<Enchantment, Integer> availableEnchantmentResults = getAvailableEnchantmentResults(modifiedLevel, stack, availableOnTable);
            if (!availableEnchantmentResults.isEmpty()) {
                Optional<Enchantment> toAdd = getRandomKey(availableEnchantmentResults, random, Enchantment::getWeight);
                Objects.requireNonNull(enchantments);
                toAdd.ifPresent(enchantment -> enchantments.put(enchantment, availableEnchantmentResults.get(enchantment)));

                while(random.nextInt(50) <= modifiedLevel) {
                    if (!enchantments.isEmpty()) {
                        filterCompatibleEnchantments(availableEnchantmentResults, getLast(enchantments));
                    }

                    if (availableEnchantmentResults.isEmpty()) {
                        break;
                    }

                    toAdd = getRandomKey(availableEnchantmentResults, random, Enchantment::getWeight);
                    Objects.requireNonNull(enchantments);
                    toAdd.ifPresent(enchantment -> enchantments.put(enchantment, availableEnchantmentResults.get(enchantment)));
                    modifiedLevel /= 2;
                }
            }

            return enchantments;
        }
    }

    private static int getModifiedLevel(RandomSource random, int level, Enchantable enchantable) {
        level += 1 + random.nextInt(enchantable.value() / 4 + 1) + random.nextInt(enchantable.value() / 4 + 1);
        float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        level = Mth.clamp(Math.round((float) level + (float) level * f), 1, Integer.MAX_VALUE);
        return level;
    }

    default void filterCompatibleEnchantments(Map<Enchantment, Integer> dataList, Enchantment enchantment) {
        List<Enchantment> toRemove = new LinkedList<>();
        for (Enchantment compare : dataList.keySet()) {
            if (compare.conflictsWith(enchantment)) {
                toRemove.add(compare);
            }
        }
        toRemove.forEach(dataList::remove);
    }

    default Map<Enchantment, Integer> getAvailableEnchantmentResults(int modifiedEnchantingLevel, ItemStack stack, Collection<Enchantment> availableOnTable) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        boolean isBook = stack.getType() == Material.BOOK;
        availableOnTable.stream()
                .filter(enchantment -> isBook || isPrimaryItem(stack, enchantment))
                .forEach((enchantment) -> {
                    for (int enchantmentLevel = getMaxLevel(enchantment); enchantmentLevel >= 1; --enchantmentLevel) {
                        if (shouldGenerateEnchant(modifiedEnchantingLevel, enchantment, enchantmentLevel)) {
                            enchantments.put(enchantment, enchantmentLevel);
                            break;
                        }
                    }
                });

        return enchantments;
    }

    default int getMaxLevel(Enchantment enchantment) {
        return enchantment.getMaxLevel();
    }

    default boolean shouldGenerateEnchant(int modifiedEnchantingLevel, Enchantment enchantment, int enchantmentLevel) {
        return modifiedEnchantingLevel >= enchantment.getMinModifiedCost(enchantmentLevel) &&
                modifiedEnchantingLevel <= enchantment.getMaxModifiedCost(enchantmentLevel);
    }

    default boolean isPrimaryItem(ItemStack item, Enchantment enchantment) {
        return EnchantUtils.isPrimaryItem(item, enchantment);
    }
}
