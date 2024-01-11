package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

public class EnderShotEnchant extends WbsEnchantment {
    private static final WbsParticleGroup EFFECT = new WbsParticleGroup().addEffect(
            new LineParticleEffect().setScaleAmount(true).setSpeed(0.05).setAmount(4), Particle.REVERSE_PORTAL
    );

    private static final WbsParticleGroup WIFF_EFFECT = new WbsParticleGroup().addEffect(
            new NormalParticleEffect().setAmount(15), Particle.SPELL_WITCH
    );

    public EnderShotEnchant() {
        super("ender_shot");
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        Entity shotEntity = event.getProjectile();

        if (!(shotEntity instanceof AbstractArrow projectile)) {
            return;
        }

        ItemStack item = event.getBow();
        if (item != null && containsEnchantment(item)) {

            Vector velocity = projectile.getVelocity();
            Location startLocation = projectile.getLocation();

            World world = projectile.getWorld();
            RayTraceResult traceResult = world.rayTrace(projectile.getLocation(),
                    velocity,
                    150, // Max distance
                    FluidCollisionMode.NEVER,
                    true,
                    0, // raySize
                    check -> !check.equals(event.getEntity()));

            if (traceResult == null) {
                WIFF_EFFECT.play(startLocation);

                event.setCancelled(true);
            } else {
                Location hitLocation = traceResult.getHitPosition().toLocation(world);
                Vector startToFinish = hitLocation.subtract(startLocation).toVector();

                double distance = startToFinish.length();
                Location teleportLocation = startLocation.clone()
                        .add(WbsMath.scaleVector(startToFinish, distance - 1));

                projectile.teleport(teleportLocation);

                Location lineStartLocation = startLocation.clone()
                        .add(WbsMath.scaleVector(startToFinish, 1));
                EFFECT.play(lineStartLocation, teleportLocation);
            }
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "Fired arrows travel instantly in a straight line to its target.";
    }

    @Override
    public String getDisplayName() {
        return "&7Ender Shot";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.VERY_RARE;
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
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
        return false;
    }
}
