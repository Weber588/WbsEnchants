package wbs.enchants.events;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Enchantable;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchants;

@SuppressWarnings("UnstableApiUsage")
public class EnchantingTableEvents implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnchantingTableAdd(PrepareItemEnchantEvent event) {
        EnchantsSettings settings = WbsEnchants.getInstance().getSettings();

        if (!settings.addEnchantability()) {
            return;
        }

        ItemStack item = event.getItem();

        Enchantable enchantableData = item.getData(DataComponentTypes.ENCHANTABLE);
        if (enchantableData == null) {
            boolean isPrimaryItem = EnchantsSettings.isPrimaryItem(item);

            if (isPrimaryItem) {
                enchantableData = Enchantable.enchantable(settings.defaultEnchantability());

                item.setData(DataComponentTypes.ENCHANTABLE, enchantableData);
            }
        }

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
