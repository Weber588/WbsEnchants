package wbs.enchants.statuseffects;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantsBootstrap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StatusEffectManager {
    private static final NamespacedKey STATUS_EFFECTS = WbsEnchantsBootstrap.createKey("status_effects");
    private static final Map<NamespacedKey, StatusEffectType> REGISTERED_TYPES = new HashMap<>();

    public static void register(StatusEffectType type) {
        REGISTERED_TYPES.put(type.getKey(), type);
    }

    @Nullable
    public static StatusEffectType getType(NamespacedKey namespacedKey) {
        return REGISTERED_TYPES.get(namespacedKey);
    }

    public static void tick(LivingEntity entity) {
        List<StatusEffect> effects = getStatusEffects(entity);
        if (effects.isEmpty()) {
            return;
        }
        List<StatusEffect> allExpired = new LinkedList<>();

        Map<StatusEffectType, StatusEffect> activeEffects = new HashMap<>();

        for (StatusEffect effect : effects) {
            effect.setDuration(effect.getDuration() - 1);

            if (effect.getDuration() > 0) {
                StatusEffect highestEffect = activeEffects.getOrDefault(effect.getType(), effect);
                if (highestEffect.getAmplifier() < effect.getAmplifier()) {
                    activeEffects.put(effect.getType(), effect);
                }
            } else {
                allExpired.add(effect);
            }
        }

        effects.removeAll(allExpired);
        for (StatusEffect expired : allExpired) {
            expired.getType().onRemove(entity, expired, StatusEffectType.RemoveReason.EXPIRED);
        }

        for (StatusEffect effect : activeEffects.values()) {
            effect.onTick(entity);
        }

        setStatusEffects(entity, effects);
    }

    public static List<StatusEffect> getStatusEffects(LivingEntity entity) {
        List<PersistentDataContainer> statusEffectContainers = getEffectContainers(entity);

        List<StatusEffect> effects = new LinkedList<>();

        if (statusEffectContainers == null || statusEffectContainers.isEmpty()) {
            return effects;
        }

        for (PersistentDataContainer statusEffectContainer : statusEffectContainers) {
            StatusEffect statusEffect = StatusEffect.DATA_TYPE.fromPrimitive(statusEffectContainer);

            effects.add(statusEffect);
        }

        return effects;
    }

    public static List<StatusEffect> getStatusEffects(LivingEntity entity, StatusEffectType type) {
        List<StatusEffect> effects = getStatusEffects(entity);

        effects.removeIf(effect -> effect.getType() != type);

        return effects;
    }

    static void addStatusEffect(LivingEntity entity, StatusEffect effect) {
        PersistentDataContainer container = entity.getPersistentDataContainer();

        List<PersistentDataContainer> statusEffectContainers = getEffectContainers(entity);
        if (statusEffectContainers == null) {
            statusEffectContainers = new LinkedList<>();
        }

        PersistentDataContainer effectContainer = StatusEffect.DATA_TYPE.toPrimitive(effect, container.getAdapterContext());
        statusEffectContainers.add(effectContainer);

        setStatusEffects(container, statusEffectContainers);
    }

    private static void setStatusEffects(PersistentDataContainer container, List<PersistentDataContainer> statusEffectContainers) {
        container.set(STATUS_EFFECTS, PersistentDataType.LIST.dataContainers(), statusEffectContainers);
    }

    static boolean hasStatusEffect(LivingEntity entity, StatusEffectType type) {
        List<PersistentDataContainer> statusEffectContainers = getEffectContainers(entity);

        if (statusEffectContainers == null || statusEffectContainers.isEmpty()) {
            return false;
        }

        for (PersistentDataContainer statusEffectContainer : statusEffectContainers) {
            StatusEffect statusEffect = StatusEffect.DATA_TYPE.fromPrimitive(statusEffectContainer);

            if (statusEffect.getType().equals(type)) {
                return true;
            }
        }

        return false;
    }

    private static @Nullable List<PersistentDataContainer> getEffectContainers(LivingEntity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();

        return container.get(STATUS_EFFECTS, PersistentDataType.LIST.dataContainers());
    }

    public static void removeStatusEffect(LivingEntity entity, StatusEffectType type, StatusEffectType.RemoveReason reason) {
        List<StatusEffect> effects = getStatusEffects(entity);

        effects.removeIf(effect -> {
            boolean matches = effect.getType().getKey().equals(type.getKey());

            if (matches) {
                effect.getType().onRemove(entity, effect, reason);
            }

            return matches;
        });

        setStatusEffects(entity, effects);
    }

    public static void clearStatusEffects(LivingEntity entity, StatusEffectType.RemoveReason reason) {
        List<StatusEffect> effects = getStatusEffects(entity);

        effects.removeIf(effect -> effect.getType().shouldRemoveOn(reason));

        setStatusEffects(entity, effects);
    }

    private static void setStatusEffects(LivingEntity entity, List<StatusEffect> effects) {
        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (effects.isEmpty()) {
            container.remove(STATUS_EFFECTS);
            return;
        }

        List<PersistentDataContainer> statusEffectContainers = new LinkedList<>();
        for (StatusEffect effect : effects) {
            PersistentDataContainer effectContainer = StatusEffect.DATA_TYPE.toPrimitive(effect, container.getAdapterContext());
            statusEffectContainers.add(effectContainer);
        }

        setStatusEffects(container, statusEffectContainers);
    }
}
