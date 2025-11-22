package wbs.enchants.enchantment.helper;

import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.bukkit.Material;
import org.bukkit.block.EnchantingTable;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
        return getEnchantments(((CraftWorld) player.getWorld()).getHandle().registryAccess(), seed, stack, salt, cost);
    }
    default Map<Enchantment, Integer> getEnchantments(RegistryAccess registryAccess, long seed, ItemStack stack, int salt, int cost) {
        RandomSource source = RandomSource.create(seed + salt);
        Optional<HolderSet.Named<net.minecraft.world.item.enchantment.Enchantment>> optional = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE);
        if (optional.isEmpty()) {
            return Map.of();
        } else {
            List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(
                    source,
                    ((CraftItemStack) stack).handle,
                    cost,
                    optional.get().stream()
            );
            if (stack.getType() == Material.BOOK && list.size() > 1) {
                list.remove(source.nextInt(list.size()));
            }

            Map<Enchantment, Integer> enchantments = new HashMap<>();
            list.forEach(instance -> {
                enchantments.put(CraftEnchantment.minecraftHolderToBukkit(instance.enchantment()), instance.level());
            });

            return enchantments;
        }
    }
}
