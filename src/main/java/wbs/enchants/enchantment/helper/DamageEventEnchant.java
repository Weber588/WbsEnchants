package wbs.enchants.enchantment.helper;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;

import java.util.function.BiConsumer;

public interface DamageEventEnchant {
    default void handleDamageEvent(Entity damager, BiConsumer<LivingEntity, Projectile> handler) {
        LivingEntity attacker = null;
        Projectile projectile = null;

        if (damager instanceof LivingEntity) {
            attacker = (LivingEntity) damager;
        } else if (damager instanceof Projectile) {
            projectile = (Projectile) damager;
            ProjectileSource source = projectile.getShooter();
            if (source instanceof LivingEntity) {
                attacker = (LivingEntity) source;
            }
        }

        if (attacker == null) {
            return;
        }

        handler.accept(attacker, projectile);
    }

}
