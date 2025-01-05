package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.enchantment.helper.TargetedDamageEnchant;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class FrostburnEnchant extends TargetedDamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "Does increased damage against water- and cold-vulnerable mobs, " +
            "like endermen and blazes, but less damage against heat-vulnerable mobs, like strays. " +
            "Has a small chance to slow enemies.";
    private static final double CHANCE_PER_LEVEL = 5;
    private static final int DURATION_PER_LEVEL = 60;

    private static WbsParticleGroup effect;

    public FrostburnEnchant() {
        super("frostburn", DEFAULT_DESCRIPTION);
        // TODO: exclusiveWith = Create #heat enchantment set (for fire aspect and for flame)

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON)
                .maxLevel(3)
                .weight(10);
    }

    @Override
    protected @NotNull Set<EntityType> getDefaultMobs() {
        return Arrays.stream(EntityType.values())
                .filter(EntityUtils::isColdVulnerable)
                .collect(Collectors.toSet());
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        super.handleAttack(event, attacker, victim, projectile);

        if (effect == null) {
            effect = new WbsParticleGroup()
                    .addEffect(new NormalParticleEffect().setXYZ(0.25).setY(0.5)
                            .setOptions(Material.PACKED_ICE.createBlockData()), Particle.BLOCK);
        }

        ItemStack item = getIfEnchanted(attacker);
        if (item != null && victim instanceof LivingEntity livingVictim) {
            int level = getLevel(item);

            if (WbsMath.chance(level * CHANCE_PER_LEVEL)) {
                effect.play(WbsEntityUtil.getMiddleLocation(victim));

                livingVictim.addPotionEffect(
                        new PotionEffect(PotionEffectType.SLOWNESS,
                                DURATION_PER_LEVEL * level,
                                level
                        )
                );
            }
        }
    }
}
