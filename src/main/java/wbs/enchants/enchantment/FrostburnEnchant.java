package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.Set;

public class FrostburnEnchant extends WbsEnchantment implements DamageEnchant {
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
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
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
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.UNCOMMON;
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
        return Set.of(FIRE_ASPECT, ARROW_FIRE);
    }

    @Override
    public Set<Enchantment> getIndirectConflicts() {
        return Set.of(DAMAGE_ALL);
    }

    @Override
    public void onLootGenerate(LootGenerateEvent event) {
        if (WbsMath.chance(10)) {
            Location location = event.getLootContext().getLocation();
            World world = location.getWorld();
            if (world == null) {
                return;
            }

            if (location.getBlock().getTemperature() < -0.5) {
                for (ItemStack stack : event.getLoot()) {
                    if (tryAdd(stack, 1)) {
                        return;
                    }
                }
            }
        }
    }
}
