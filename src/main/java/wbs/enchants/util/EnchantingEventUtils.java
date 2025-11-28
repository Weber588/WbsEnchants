package wbs.enchants.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.events.enchanting.*;

import java.util.*;
import java.util.function.ToIntFunction;

@SuppressWarnings("UnstableApiUsage")
public class EnchantingEventUtils {
    public static final int DEFAULT_MAX_POWER = 15;

    public static int getEnchantmentCost(long seed, int slot, int power) {
        return getEnchantmentCost(seed, slot, power, power);
    }
    public static int getEnchantmentCost(long seed, int slot, int power, int maxPower) {
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

    public static List<Block> getPowerProviderBlocks(Block table) {
        List<Block> providers = new LinkedList<>();

        World world = table.getWorld();
        BlockPos tablePos = ((CraftBlock) table).getPosition();
        for (BlockPos offsetPos : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (EnchantingTableBlock.isValidBookShelf(((CraftWorld) world).getHandle(), tablePos, offsetPos)) {
                providers.add(world.getBlockAt(tablePos.getX() + offsetPos.getX(), tablePos.getY() + offsetPos.getY(), tablePos.getZ() + offsetPos.getZ()));
            }
        }

        return providers;
    }

    public static void updateEnchantmentOffers(PrepareItemEnchantEvent event, EnchantingPreparationContext context, long seed) {
        updateEnchantmentOffers(event, context, seed, 0);
    }

    public static void updateEnchantmentOffers(PrepareItemEnchantEvent event, EnchantingPreparationContext context, long seed, int salt) {
        @Nullable EnchantmentOffer[] offers = event.getOffers();
        int[] costs = new int[offers.length];

        for (int cost = 0; cost < offers.length; cost++) {
            EnchantmentOffer offer = offers[cost];
            if (offer != null) {
                costs[cost] = offer.getCost();
            }
        }

        updateEnchantmentOffers(event, context, seed, salt, costs);
    }
    public static void updateEnchantmentOffers(PrepareItemEnchantEvent event, EnchantingPreparationContext context, long seed, int salt, int[] costs) {
        ItemStack item = event.getItem();

        seed += salt;

        Random random = new Random(seed);

        for (int slot = 0; slot < 3; slot++) {
            EnchantmentOffer offer = event.getOffers()[slot];
            if (offer != null) {
                Map<Enchantment, Integer> enchantments = getEnchantments(
                        context,
                        seed,
                        item,
                        slot,
                        costs[slot]
                );

                if (enchantments.isEmpty()) {
                    continue;
                }

                Enchantment[] sortedOptions = enchantments.keySet().stream()
                        .sorted(Comparator.comparing(Keyed::getKey))
                        .toArray(Enchantment[]::new);

                Enchantment chosenEnchantment = sortedOptions[random.nextInt(sortedOptions.length)];

                ChooseEnchantmentHintEvent chooseHintEvent = new ChooseEnchantmentHintEvent(context, chosenEnchantment, enchantments);
                chooseHintEvent.callEvent();

                offer.setEnchantment(chooseHintEvent.getChosenEnchantment());
                offer.setEnchantmentLevel(enchantments.get(chooseHintEvent.getChosenEnchantment()));
            }
        }
    }

    public static Map<Enchantment, Integer> getEnchantments(EnchantingContext context, long seed, ItemStack stack, int slot, int cost) {
        RandomSource source = RandomSource.create(seed + slot);

        Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Tag<@NotNull Enchantment> availableOnTableTag = registry.getTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE);
        Collection<@NotNull Enchantment> availableOnTable = availableOnTableTag.resolve(registry);

        if (availableOnTable.isEmpty()) {
            return Map.of();
        } else {
            Map<Enchantment, Integer> enchantments = selectEnchantment(
                    context,
                    source,
                    stack,
                    cost,
                    availableOnTable
            );

            if (stack.getType() == Material.BOOK && enchantments.size() > 1) {
                Enchantment key = getRandomKey(enchantments, source);

                enchantments.remove(key);
            }

            SelectEnchantmentsEvent selectionEvent = new SelectEnchantmentsEvent(context, enchantments, cost, slot);
            selectionEvent.callEvent();

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

    public static Map<Enchantment, Integer> selectEnchantment(EnchantingContext context, RandomSource random, ItemStack stack, final int level, Collection<Enchantment> availableOnTable) {
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
        Enchantable enchantable = stack.getData(DataComponentTypes.ENCHANTABLE);
        if (enchantable == null) {
            return enchantments;
        } else {
            int modifiedLevel = getModifiedLevel(random, level, enchantable);
            Map<Enchantment, Integer> availableEnchantmentResults = getAvailableEnchantmentResults(context, modifiedLevel, stack, availableOnTable);
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

                    toAdd = getRandomKey(availableEnchantmentResults, random, weighted -> getWeight(context, weighted));
                    Objects.requireNonNull(enchantments);
                    toAdd.ifPresent(enchantment -> enchantments.put(enchantment, availableEnchantmentResults.get(enchantment)));
                    modifiedLevel /= 2;
                }
            }

            return enchantments;
        }
    }

    private static int getWeight(EnchantingContext context, Enchantment enchantment) {
        int weight = enchantment.getWeight();

        DeriveEnchantmentWeightEvent event = new DeriveEnchantmentWeightEvent(context, enchantment, weight);
        event.callEvent();

        return event.getWeight();
    }

    private static int getModifiedLevel(RandomSource random, int level, Enchantable enchantable) {
        level += 1 + random.nextInt(enchantable.value() / 4 + 1) + random.nextInt(enchantable.value() / 4 + 1);
        float f = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        level = Mth.clamp(Math.round((float) level + (float) level * f), 1, Integer.MAX_VALUE);
        return level;
    }

    public static void filterCompatibleEnchantments(Map<Enchantment, Integer> enchantments, Enchantment enchantment) {
        List<Enchantment> toRemove = new LinkedList<>();
        for (Enchantment compare : enchantments.keySet()) {
            if (!compare.equals(enchantment) && compare.conflictsWith(enchantment)) {
                toRemove.add(compare);
            }
        }
        toRemove.forEach(enchantments::remove);
    }

    public static Map<Enchantment, Integer> getAvailableEnchantmentResults(EnchantingContext context, int modifiedEnchantingLevel, ItemStack stack, Collection<Enchantment> availableOnTable) {
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        boolean isBook = stack.getType() == Material.BOOK;
        Set<Enchantment> available = new HashSet<>(availableOnTable);

        available.removeIf(enchantment -> !(isBook || EnchantUtils.isPrimaryItem(stack, enchantment)));

        GetAvailableEnchantsEvent availableEnchantsEvent = new GetAvailableEnchantsEvent(context, available, stack);
        availableEnchantsEvent.callEvent();

        for (Enchantment enchantment : available) {
            if (enchantments.containsKey(enchantment)) {
                continue;
            }
            for (int enchantmentLevel = enchantment.getMaxLevel(); enchantmentLevel >= 1; --enchantmentLevel) {
                if (shouldGenerateEnchant(context, modifiedEnchantingLevel, enchantment, enchantmentLevel)) {
                    enchantments.put(enchantment, enchantmentLevel);
                    break;
                }
            }
        }

        return enchantments;
    }

    public static boolean shouldGenerateEnchant(EnchantingContext context, int modifiedEnchantingLevel, Enchantment enchantment, int enchantmentLevel) {
        boolean isAllowed = modifiedEnchantingLevel >= enchantment.getMinModifiedCost(enchantmentLevel) &&
                modifiedEnchantingLevel <= enchantment.getMaxModifiedCost(enchantmentLevel);

        EnchantmentGenerationCheckEvent event = new EnchantmentGenerationCheckEvent(context, modifiedEnchantingLevel, enchantment, enchantmentLevel, isAllowed);
        event.callEvent();

        return event.isAllowed();
    }
}

