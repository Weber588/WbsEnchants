package wbs.enchants.statuseffects;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;

public abstract class StatusEffectType implements Keyed {
    private static final WbsParticleEffect DEFAULT_PARTICLE_EFFECT = new NormalParticleEffect()
            .setX(1)
            .setY(2)
            .setZ(1);

    @NotNull
    private final NamespacedKey key;
    protected WbsParticleEffect particles;
    protected int ticksPerParticle;

    public StatusEffectType(@NotNull NamespacedKey key) {
        this.key = key;
    }

    StatusEffectType(String keyValue) {
        this(WbsEnchantsBootstrap.createKey(keyValue));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public void onApply(LivingEntity entity, StatusEffect effect) {

    }

    public void onTick(LivingEntity entity, StatusEffect effect) {

    }

    public void onRemove(LivingEntity entity, StatusEffect effect, RemoveReason reason) {

    }

    public boolean shouldRemoveOn(RemoveReason reason) {
        return true;
    }

    public enum RemoveReason {
        EXPIRED,
        DEATH,
        MILK,
        CUSTOM
    }
}
