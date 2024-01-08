package wbs.enchants.enchantment;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;

public abstract class AbstractDamageEnchant extends WbsEnchantment {
    public AbstractDamageEnchant(String key) {
        super(key);
    }

    @SuppressWarnings("unused")
    protected final void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (!(entity instanceof Damageable victim)) return;

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

        handleAttack(event, attacker, victim, projectile);
    }

    public abstract void catchEvent(EntityDamageByEntityEvent event);
    protected abstract void handleAttack(@NotNull EntityDamageByEntityEvent event,
                                         @NotNull LivingEntity attacker,
                                         @NotNull Entity victim,
                                         @Nullable Projectile projectile);
}
