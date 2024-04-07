package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FrostburnEnchant extends TargetedDamageEnchant {
    private static final double CHANCE_PER_LEVEL = 5;
    private static final int DURATION_PER_LEVEL = 60;

    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setXYZ(0.25).setY(0.5)
                    .setOptions(Material.PACKED_ICE.createBlockData()), Particle.BLOCK_CRACK);

    public FrostburnEnchant() {
        super("frostburn");
    }

    @Override
    public @NotNull String getDescription() {
        return "Does increased damage against water- and cold-vulnerable mobs, like endermen and blazes, but less " +
                "damage against heat-vulnerable mobs, like strays. Has a small chance to slow enemies.";
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

        ItemStack item = getIfEnchanted(attacker);
        if (item != null && victim instanceof LivingEntity livingVictim) {
            int level = getLevel(item);

            if (WbsMath.chance(level * CHANCE_PER_LEVEL)) {
                EFFECT.play(WbsEntityUtil.getMiddleLocation(victim));

                livingVictim.addPotionEffect(
                        new PotionEffect(PotionEffectType.SLOW,
                                DURATION_PER_LEVEL * level,
                                level
                        )
                );
            }
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Frostburn";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public Set<Enchantment> getDirectConflicts() {
        Set<Enchantment> directConflicts = new HashSet<>(super.getDirectConflicts());
        directConflicts.addAll(Set.of(FIRE_ASPECT, ARROW_FIRE));
        return directConflicts;
    }
}
