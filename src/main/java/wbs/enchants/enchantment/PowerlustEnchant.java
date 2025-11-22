package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.EnchantmentView;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.EnchantingTableEnchant;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class PowerlustEnchant extends WbsEnchantment implements EnchantingTableEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Makes all 3 offered enchantments use the highest level that " +
            "the enchanting table can offer.";

    public PowerlustEnchant() {
        super("powerlust", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(ItemTypeKeys.ENCHANTING_TABLE)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_ENCHANTING_TABLE);
    }

    @EventHandler
    public void onPrepEnchant(PrepareItemEnchantEvent event) {
        Integer level = getLevel(event.getEnchantBlock());
        if (level == null) {
            return;
        }

        if (event.getItem().isEmpty()) {
            return;
        }

        int seed = event.getView().getEnchantmentSeed();

        for (int slot = 0; slot < event.getOffers().length; slot++) {
            EnchantmentOffer offer = event.getOffers()[slot];
            if (offer != null) {
                int cost = getEnchantmentCost(seed + slot,
                        2,
                        event.getEnchantmentBonus());

                offer.setCost(cost);
            }
        }

        updateEnchantmentOffers(event);
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

        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        enchantsToAdd.clear();
        enchantsToAdd.putAll(enchantments);
    }
}
