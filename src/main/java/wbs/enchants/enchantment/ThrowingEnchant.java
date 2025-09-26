package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

public class ThrowingEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Items thrown (potions, eggs, ender pearls) have increased speed.";

    private static final double PERCENT_BOOST_PER_LEVEL = 25;

    public ThrowingEnchant() {
        super("throwing", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(4)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_CHEST_ARMOR);
    }

    @EventHandler
    public void onThrow(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof ThrowableProjectile projectile)) {
            return;
        }

        if (projectile.getShooter() instanceof LivingEntity shooter) {
            int totalLevels = getSumLevelsArmour(shooter);

            if (totalLevels <= 0) {
                return;
            }

            Vector velocity = projectile.getVelocity();
            velocity.multiply(1 + (totalLevels * PERCENT_BOOST_PER_LEVEL / 100));

            projectile.setVelocity(velocity);
        }
    }
}
