package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.util.EnchantUtils;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

public class FrostburnEnchant extends AbstractDamageEnchant {
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

    @EventHandler
    @Override
    public void catchEvent(EntityDamageByEntityEvent event) {
        onDamage(event);
    }

    @Override
    protected void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        EntityEquipment equipment = attacker.getEquipment();
        if (equipment == null) {
            return;
        }
        
        ItemStack item = equipment.getItemInMainHand();
        if (containsEnchantment(item)) {
            double initialDamage = event.getDamage();
            double damage = initialDamage;
            int level = getLevel(item);

            EFFECT.play(WbsEntityUtil.getMiddleLocation(livingVictim));
            if (damage > 0) {
                if (WbsMath.chance(level * CHANCE_PER_LEVEL)) {
                    livingVictim.addPotionEffect(
                            new PotionEffect(PotionEffectType.SLOW,
                                    DURATION_PER_LEVEL * level,
                                    level
                            )
                    );
                }
            }

            if (EntityUtils.isColdVulnerable(livingVictim.getType())) {
                damage += 2.5 * level; // Smite-like calculation
            } else if (EntityUtils.isHotVulnerable(livingVictim.getType())) {
                damage -= level;
            } else {
                return;
            }

            if (initialDamage <= 0) {
                event.setDamage(damage);
            } else {
                event.setDamage(Math.max(1, damage));
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
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return enchantment == FIRE_ASPECT ||
                enchantment == ARROW_FIRE ||
                EnchantUtils.willConflict(DAMAGE_ALL, enchantment);
    }
}
