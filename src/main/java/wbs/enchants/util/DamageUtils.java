package wbs.enchants.util;

import org.bukkit.event.entity.EntityDamageEvent;

public class DamageUtils {
    public static boolean canBeBlocked(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, PROJECTILE, BLOCK_EXPLOSION, ENTITY_EXPLOSION, THORNS, CUSTOM -> true;
            default -> false;
        };

    }

}
