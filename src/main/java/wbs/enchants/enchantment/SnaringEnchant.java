package wbs.enchants.enchantment;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

public class SnaringEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Pulls hit entities towards you; the opposite of Knockback.";

    public SnaringEnchant() {
        super("snaring", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_KNOCKBACK)
                .maxLevel(2)
                .weight(10);
    }

    @EventHandler
    public void onKnockback(EntityKnockbackByEntityEvent event) {
        if (!(event.getHitBy() instanceof LivingEntity attacker)) {
            return;
        }

        ItemStack item = getIfEnchanted(attacker);

        if (item != null && isEnchantmentOn(item)) {
            int level = getLevel(item);
            double multiplier = Math.max(1, level * 0.6);

            Vector originalKnockback = event.getKnockback();
            event.setKnockback(originalKnockback.clone().multiply(-multiplier).setY(originalKnockback.getY()));
        }
    }
}
