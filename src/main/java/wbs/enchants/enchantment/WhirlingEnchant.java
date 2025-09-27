package wbs.enchants.enchantment;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

public class WhirlingEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Converts knockback to be sideways.";

    public WhirlingEnchant() {
        super("whirling", DEFAULT_DESCRIPTION);

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

        if (event.getCause() == EntityKnockbackEvent.Cause.SWEEP_ATTACK) {
            return;
        }

        ItemStack item = getIfEnchanted(attacker);

        if (item != null && isEnchantmentOn(item)) {
            int level = getLevel(item);
            double multiplier = Math.max(1, level * 0.75);

            Vector knockback = event.getKnockback().rotateAroundY(Math.PI / 2);
            if (attacker instanceof Player player && player.getMainHand() == MainHand.LEFT) {
                knockback.multiply(-1);
            }
            event.setKnockback(knockback.multiply(multiplier));
        }
    }
}
