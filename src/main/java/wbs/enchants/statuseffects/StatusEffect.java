package wbs.enchants.statuseffects;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.statuseffects.serialization.PersistentDataContainerSerializable;
import wbs.enchants.statuseffects.serialization.PersistentDataContainerSerializer;

import java.util.UUID;

public class StatusEffect implements PersistentDataContainerSerializable {
    private static final NamespacedKey TYPE_KEY = WbsEnchantsBootstrap.createKey("type");
    private static final NamespacedKey DURATION_KEY = WbsEnchantsBootstrap.createKey("duration");
    private static final NamespacedKey AMPLIFIER_KEY = WbsEnchantsBootstrap.createKey("amplifier");
    private static final NamespacedKey ENCHANTMENT_KEY = WbsEnchantsBootstrap.createKey("source_enchantment");
    private static final NamespacedKey ENTITY_KEY = WbsEnchantsBootstrap.createKey("source_entity");

    public static final PersistentDataContainerSerializer<StatusEffect> DATA_TYPE = new PersistentDataContainerSerializer<>(
            StatusEffect.class,
            (container, adapter) -> {
                String typeKeyString = container.get(TYPE_KEY, PersistentDataType.STRING);
                if (typeKeyString == null) {
                    throw new IllegalStateException("Type key cannot be null.");
                }

                StatusEffectType type = StatusEffectManager.getType(NamespacedKey.fromString(typeKeyString));
                if (type == null) {
                    throw new IllegalStateException("Type not found for key \"%s\".".formatted(typeKeyString));
                }

                Builder builder = new Builder(type);

                Integer duration = container.get(DURATION_KEY, PersistentDataType.INTEGER);
                if (duration != null) {
                    builder.setDuration(duration);
                }

                Integer amplifier = container.get(AMPLIFIER_KEY, PersistentDataType.INTEGER);
                if (amplifier != null) {
                    builder.setAmplifier(amplifier);
                }

                String enchantKeyString = container.get(ENCHANTMENT_KEY, PersistentDataType.STRING);
                if (enchantKeyString != null) {
                    WbsEnchantment enchantment = EnchantManager.getCustomFromKey(NamespacedKey.fromString(typeKeyString));
                    builder.setEnchantmentSource(enchantment);
                }

                String entityUUIDString = container.get(ENTITY_KEY, PersistentDataType.STRING);
                if (entityUUIDString != null) {
                    UUID entityUUID = UUID.fromString(entityUUIDString);
                    Entity entity = Bukkit.getEntity(entityUUID);
                    builder.setEntitySource(entity);
                }

                return builder.build();
            }
    );

    @NotNull
    private final StatusEffectType type;
    private int duration;
    private final int amplifier;
    @Nullable
    private final WbsEnchantment enchantmentSource;
    @Nullable
    private final Entity entitySource;

    public StatusEffect(@NotNull StatusEffectType type, int duration, int amplifier, @Nullable WbsEnchantment enchantmentSource, @Nullable Entity entitySource) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.enchantmentSource = enchantmentSource;
        this.entitySource = entitySource;
    }

    public void applyTo(LivingEntity entity) {
        StatusEffectManager.addStatusEffect(entity, this);
        type.onApply(entity, this);
    }

    public void onTick(LivingEntity entity) {
        type.onTick(entity, this);
    }

    @Override
    public void populate(PersistentDataContainer container) {
        container.set(TYPE_KEY, PersistentDataType.STRING, type.getKey().asString());
        container.set(DURATION_KEY, PersistentDataType.INTEGER, duration);
        container.set(AMPLIFIER_KEY, PersistentDataType.INTEGER, amplifier);
        if (enchantmentSource != null) {
            container.set(ENCHANTMENT_KEY, PersistentDataType.STRING, enchantmentSource.getKey().asString());
        }
        if (entitySource != null) {
            container.set(ENTITY_KEY, PersistentDataType.STRING, entitySource.getUniqueId().toString());
        }
    }

    public @NotNull StatusEffectType getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public StatusEffect setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public @Nullable WbsEnchantment getEnchantmentSource() {
        return enchantmentSource;
    }

    public @Nullable Entity getEntitySource() {
        return entitySource;
    }

    public static class Builder {
        @NotNull
        private StatusEffectType type;
        private int duration;
        private int amplifier = 0;
        @Nullable
        private WbsEnchantment enchantmentSource;
        @Nullable
        private Entity entitySource;

        public Builder(@NotNull StatusEffectType type) {
            this.type = type;
        }

        public Builder setType(StatusEffectType type) {
            this.type = type;
            return this;
        }

        public Builder setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder setAmplifier(int amplifier) {
            this.amplifier = amplifier;
            return this;
        }

        public Builder setEnchantmentSource(@Nullable WbsEnchantment enchantmentSource) {
            this.enchantmentSource = enchantmentSource;
            return this;
        }

        public Builder setEntitySource(@Nullable Entity entitySource) {
            this.entitySource = entitySource;
            return this;
        }

        public StatusEffect build() {
            return new StatusEffect(type, duration, amplifier, enchantmentSource, entitySource);
        }
    }
}
