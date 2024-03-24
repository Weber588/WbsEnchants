package wbs.enchants.events;

import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.jetbrains.annotations.Nullable;

public class EnchantEvents implements Listener {
    @EventHandler
    public void onOfferEnchant(PrepareItemEnchantEvent event) {
        @Nullable EnchantmentOffer[] offers = event.getOffers();
        for (EnchantmentOffer offer : event.getOffers()) {
            if (offer != null) {

            }
        }
    }
}
