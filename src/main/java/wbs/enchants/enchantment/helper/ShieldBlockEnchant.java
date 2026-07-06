package wbs.enchants.enchantment.helper;

import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.util.EventUtils;

public interface ShieldBlockEnchant extends DamageEventEnchant, EnchantInterface, AutoRegistrableEnchant {
    default void registerShieldBlockEvent() {
        EventUtils.register(EntityDamageByEntityEvent.class, this::onBlockDamage);
    }

    default void onBlockDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!victim.isBlocking()) {
            return;
        }

        handleDamageEvent(damager, (attacker, projectile) -> {
            handleBlockDamage(event, attacker, victim, projectile);
        });
    }

    void handleBlockDamage(@NotNull EntityDamageByEntityEvent event,
                           @NotNull LivingEntity attacker,
                           @NotNull Player victim,
                           @Nullable Projectile projectile);
}
