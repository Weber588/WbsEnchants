package wbs.enchants.enchantment.helper;

import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.util.EventUtils;

public interface SpongeEnchant extends EnchantInterface, AutoRegistrableEnchant {
    default void registrySpongeEvents() {
        EventUtils.register(FurnaceSmeltEvent.class, this::onDryFurnace, EventPriority.LOW);
    }

    default void onDryFurnace(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();
        if (getThisEnchantment().isEnchantmentOn(source)) {
            ItemStack result = event.getResult();

            result.setItemMeta(source.getItemMeta());
        }
    }

}
