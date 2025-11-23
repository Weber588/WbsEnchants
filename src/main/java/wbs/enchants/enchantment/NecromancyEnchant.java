package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.craftbukkit.entity.CraftMob;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.NormalParticleEffect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NecromancyEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Kills with this weapon may convert undead mobs into living ones.";
    private static final double CHANCE_PER_LEVEL = 30;

    private final Map<NamespacedKey, NamespacedKey> entityTypeMap = new HashMap<>();

    public NecromancyEnchant() {
        super("necromancy", DEFAULT_DESCRIPTION);

        registerTransformation("zombie_villager", "villager");
        registerTransformation("zombie_horse", "horse");
        registerTransformation("skeleton_horse", "horse");
        registerTransformation("zoglin", "hoglin");
        registerTransformation("zombified_piglin", "piglin");

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON)
                .activeSlots(EquipmentSlotGroup.MAINHAND)
                .maxLevel(3)
                .weight(1)
                .anvilCost(6);
    }

    private void registerTransformation(String from, String to) {
        entityTypeMap.put(NamespacedKey.minecraft(from), NamespacedKey.minecraft(to));
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        NamespacedKey transformToKey = entityTypeMap.get(entity.getType().getKey());
        if (transformToKey == null) {
            return;
        }

        EntityType transformTo = Arrays.stream(EntityType.values()).filter(type -> type.getKey().equals(transformToKey)).findAny().orElse(null);
        if (transformTo == null) {
            WbsEnchants.getInstance().getLogger().warning("Invalid mob transformation configured for " + key().asMinimalString() + "; " + transformToKey.asMinimalString());
            return;
        }

        Player killer = entity.getKiller();
        if (killer != null) {
            ItemStack item = getIfEnchanted(killer, EquipmentSlot.HAND);

            if (item != null) {
                int level = getLevel(item);

                if (WbsMath.chance(CHANCE_PER_LEVEL * level)) {
                    if (entity instanceof CraftMob craftMob && craftMob.getHandle() instanceof Mob mob) {
                        net.minecraft.world.entity.EntityType<?> entityType = CraftEntityType.bukkitToMinecraft(transformTo);
                        if (Mob.class.isAssignableFrom(entityType.getBaseClass())) {
                            //noinspection unchecked
                            convertMob(entity, mob, (net.minecraft.world.entity.EntityType<? extends Mob>) entityType);
                        }
                    }
                }
            }
        }
    }

    private static <T extends Mob> void convertMob(LivingEntity bukkitEntity, Mob nmsMob, net.minecraft.world.entity.EntityType<T> entityType) {
        nmsMob.convertTo(
                entityType,
                ConversionParams.single(nmsMob, true, false),
                EntitySpawnReason.CONVERSION,
                result -> {
                    new NormalParticleEffect()
                            .setXYZ(0)
                            .setSpeed(0.5)
                            .setAmount(150)
                            .play(Particle.TOTEM_OF_UNDYING, bukkitEntity.getLocation());
                },
                EntityTransformEvent.TransformReason.CURED,
                CreatureSpawnEvent.SpawnReason.CURED
        );
    }
}
