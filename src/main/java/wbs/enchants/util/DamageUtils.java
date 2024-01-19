package wbs.enchants.util;

import org.bukkit.event.entity.EntityDamageEvent;

public class DamageUtils {
    public static boolean canBeBlocked(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, PROJECTILE, BLOCK_EXPLOSION, ENTITY_EXPLOSION, THORNS,
                    CUSTOM -> true;
            default -> false;
        };
    }

    public static boolean isHeat(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case FIRE, FIRE_TICK, HOT_FLOOR, LAVA -> true;
            default -> false;
        };
    }

    public static boolean isPhysical(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, CONTACT, CRAMMING, FALL, FALLING_BLOCK, FLY_INTO_WALL,
                    THORNS -> true;
            default -> false;
        };
    }

    public static boolean isEnergy(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case MAGIC, LIGHTNING, DRAGON_BREATH, WITHER -> true;
            default -> false;
        };
    }

    public static boolean isUnstoppable(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case KILL, VOID, WORLD_BORDER -> true;
            default -> false;
        };
    }
}
