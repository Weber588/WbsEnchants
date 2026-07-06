package wbs.enchants.enchantment.helper;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.util.EventUtils;

public interface DamageEnchant extends DamageEventEnchant, EnchantInterface, AutoRegistrableEnchant {
    default void registerDamageEvent() {
        EventUtils.register(EntityDamageByEntityEvent.class, this::onDamage, getEventPriority(), ignoreCancelled());
    }

    default void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (!(entity instanceof Damageable victim)) return;

        handleDamageEvent(damager, (attacker, projectile) -> {
            handleAttack(event, attacker, victim, projectile);
        });
    }

    void handleAttack(@NotNull EntityDamageByEntityEvent event,
                                         @NotNull LivingEntity attacker,
                                         @NotNull Entity victim,
                                         @Nullable Projectile projectile);

    default boolean ignoreCancelled() {
        return true;
    }

    default @NotNull EventPriority getEventPriority() {
        return EventPriority.NORMAL;
    }
}
