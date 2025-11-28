package wbs.enchants.enchantment;

import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.EnchantmentView;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.EnchantingTableEnchant;

import java.util.Map;
import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class AmbitiousnessEnchant extends WbsEnchantment implements EnchantingTableEnchant {
    private static final double MAX_LEVEL_BREAK_CHANCE = 50;

    private static final @NotNull String DEFAULT_DESCRIPTION = "Increases the maximum enchanting level, and allows " +
            "you to exceed the maximum level for some enchantments.";

    public AmbitiousnessEnchant() {
        super("ambitiousness", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.ENCHANTING_TABLE)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_ENCHANTING_TABLE);
    }

    @EventHandler
    public void onOpenEnchantingTable(InventoryOpenEvent event) {
        if (!(event.getView() instanceof EnchantmentView view)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof TileStateInventoryHolder holder)) {
            return;
        }

        if (!isEnchanted(holder.getBlock())) {
            return;
        }

        HumanEntity player = event.getPlayer();
        // Add hashcode so enchants on tables of this enchantment won't just be a copy of vanilla enchants but better levels.
        view.setEnchantmentSeed(new Random(player.getEnchantmentSeed() + hashCode()).nextInt());
    }

    @EventHandler
    public void onPrepEnchant(PrepareItemEnchantEvent event) {
        Integer level = getLevel(event.getEnchantBlock());
        if (level == null) {
            return;
        }

        int seed = event.getView().getEnchantmentSeed();

        for (int slot = 0; slot < event.getOffers().length; slot++) {
            EnchantmentOffer offer = event.getOffers()[slot];
            if (offer != null) {
                offer.setCost(getEnchantmentCost(seed,
                        slot,
                        event.getEnchantmentBonus(),
                        getMaxPower(level))
                );
            }
        }

        updateEnchantmentOffers(event, seed);
        int itemHash = getItemHash(event.getItem());

        for (int slot = 0; slot < event.getOffers().length; slot++) {
            EnchantmentOffer offer = event.getOffers()[slot];
            if (offer != null) {
                Enchantment enchantment = offer.getEnchantment();
                if (enchantment.getMaxLevel() > 1 && shouldBreakMaxLevel(seed + slot + itemHash + enchantment.getKey().hashCode(), level, offer.getCost())) {
                    offer.setEnchantmentLevel(offer.getEnchantmentLevel() + 1);
                }
            }
        }
    }

    private int getItemHash(@NotNull ItemStack item) {
        return item.getItemMeta().getAsComponentString().hashCode();
    }

    private static int getMaxCost(int level) {
        return getMaxPower(level) * 2;
    }

    private static int getMaxPower(int level) {
        return DEFAULT_MAX_POWER + 5 * level;
    }

    private boolean shouldBreakMaxLevel(int seed, int tableLevel, int cost) {
        double chanceFromLevel = ((double) tableLevel / maxLevel()) / 2;
        double chanceFromCost = ((double) cost / getMaxCost(tableLevel)) / 2;

        double chance = (chanceFromCost + chanceFromLevel) * (MAX_LEVEL_BREAK_CHANCE / 100);

        return new Random(seed).nextDouble() > chance;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Integer level = getLevel(event.getEnchantBlock());
        if (level == null) {
            return;
        }

        int seed = ((EnchantmentView) event.getView()).getEnchantmentSeed();

        int cost = event.getExpLevelCost();
        ItemStack item = event.getItem();

        Map<Enchantment, Integer> enchantments = getEnchantments(
                event.getEnchanter(),
                seed,
                item,
                event.whichButton(),
                cost
        );

        int itemHash = getItemHash(event.getItem());

        for (Enchantment enchantment : enchantments.keySet()) {
            int enchantLevel = enchantments.get(enchantment);
            if (enchantment.getMaxLevel() > 1 && shouldBreakMaxLevel(seed + event.whichButton() + itemHash + enchantment.getKey().hashCode(), level, cost)) {
                enchantments.put(enchantment, enchantLevel + 1);
            }
        }

        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        enchantsToAdd.clear();
        enchantsToAdd.putAll(enchantments);
    }

    @Override
    public boolean shouldGenerateEnchant(int modifiedEnchantingLevel, Enchantment enchantment, int enchantmentLevel) {
        if (enchantment.getMaxLevel() == enchantmentLevel) {
            WbsEnchants.getInstance().getLogger().info("shouldGenerateEnchant: " + enchantment.getKey().asMinimalString() + " " + modifiedEnchantingLevel + "; " + modifiedEnchantingLevel);
            WbsEnchants.getInstance().getLogger().info("enchantment.getMinModifiedCost(enchantmentLevel): " + enchantment.getMinModifiedCost(enchantmentLevel));
        }
        // Remove upper bounce for enchant types
        return modifiedEnchantingLevel >= enchantment.getMinModifiedCost(enchantmentLevel);
    }
}
