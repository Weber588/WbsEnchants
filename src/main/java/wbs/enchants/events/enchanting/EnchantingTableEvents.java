package wbs.enchants.events.enchanting;

import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.EnchantmentView;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.EnchantingEventUtils;

import java.util.Map;
import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class EnchantingTableEvents implements Listener {
    @EventHandler
    public void onOpenEnchantingTable(InventoryOpenEvent event) {
        if (!(event.getView() instanceof EnchantmentView view)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof TileStateInventoryHolder)) {
            return;
        }

        HumanEntity player = event.getPlayer();
        // Add hashcode so enchants on tables of this enchantment won't just be a copy of vanilla enchants but vaguely modified.
        view.setEnchantmentSeed(new Random(player.getEnchantmentSeed() + hashCode()).nextInt());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void prepareOverride(PrepareItemEnchantEvent event) {
        int seed = event.getView().getEnchantmentSeed();

        for (int slot = 0; slot < event.getOffers().length; slot++) {
            EnchantmentOffer offer = event.getOffers()[slot];
            if (offer != null) {
                EnchantingPreparationContext context = new EnchantingPreparationContext(
                        event.getEnchantBlock(),
                        event.getEnchanter(),
                        event.getItem(),
                        seed,
                        slot
                );

                int power = getPower(event, context);

                int cost = EnchantingEventUtils.getEnchantmentCost(
                        seed,
                        slot,
                        power
                );

                ChooseEnchantmentCostEvent chooseCostEvent = new ChooseEnchantmentCostEvent(context, seed, slot, power, cost);
                chooseCostEvent.callEvent();

                offer.setCost(chooseCostEvent.getCost());
            }
        }

        EnchantingPreparationContext context = new EnchantingPreparationContext(event.getEnchantBlock(), event.getEnchanter(), event.getItem(), seed, null);

        EnchantingEventUtils.updateEnchantmentOffers(event, context, seed);
    }

    private static int getPower(PrepareItemEnchantEvent event, EnchantingPreparationContext context) {
        EnchantingDerivePowerEvent choosePowerEvent = new EnchantingDerivePowerEvent(
                context,
                event.getEnchantmentBonus(),
                EnchantingEventUtils.DEFAULT_MAX_POWER
        );
        choosePowerEvent.callEvent();

        return Math.min(choosePowerEvent.getPower(), choosePowerEvent.getMaxPower());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void enchantOverride(EnchantItemEvent event) {
        int seed = ((EnchantmentView) event.getView()).getEnchantmentSeed();

        int cost = event.getExpLevelCost();
        ItemStack item = event.getItem();

        EnchantingEnchantContext context = new EnchantingEnchantContext(event.getEnchantBlock(), event.getEnchanter(), event.getItem(), seed, event.whichButton(), cost);

        Map<Enchantment, Integer> enchantments = EnchantingEventUtils.getEnchantments(
                context,
                seed,
                item,
                event.whichButton(),
                cost
        );

        FinalizeItemEnchantmentsEvent finalizeEvent = new FinalizeItemEnchantmentsEvent(context, event, enchantments);
        finalizeEvent.callEvent();

        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        enchantsToAdd.clear();
        enchantsToAdd.putAll(enchantments);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnchantingTableAdd(PrepareItemEnchantEvent event) {
        EnchantsSettings settings = WbsEnchants.getInstance().getSettings();

        ItemStack item = event.getItem();

        if (settings.addEnchantability()) {
            Enchantable enchantableData = item.getData(DataComponentTypes.ENCHANTABLE);
            if (enchantableData == null) {
                boolean isPrimaryItem = EnchantsSettings.isPrimaryItem(item);

                if (isPrimaryItem) {
                    enchantableData = Enchantable.enchantable(settings.defaultEnchantability());

                    item.setData(DataComponentTypes.ENCHANTABLE, enchantableData);
                }
            }
        }

        validatePreparation(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void validatePreparation(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();

        boolean hasValidOffer = false;

        @Nullable EnchantmentOffer[] offers = event.getOffers();

        // If any offers are null, shuffle offers down because of a client side bug that always renders level 1 even if no enchant.
        for (int i = 0; i < offers.length; i++) {
            EnchantmentOffer offer = offers[i];

            if (offer == null) {
                if (i < 2) {
                    offers[i] = offers[i + 1];
                }
                if (i < 1) {
                    offers[i + 1] = offers[i + 2];
                }

                offers[2] = null;
            }

            offer = offers[i];
            if (offer != null) {
                hasValidOffer = true;
            }
        }

        if (hasValidOffer && item.hasData(DataComponentTypes.ENCHANTABLE) && item.getEnchantments().isEmpty()) {
            event.setCancelled(false);
        }
    }
}
