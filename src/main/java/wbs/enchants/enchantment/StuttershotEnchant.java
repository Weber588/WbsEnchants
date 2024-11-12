package wbs.enchants.enchantment;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ProjectileEnchant;
import wbs.utils.util.WbsMath;

public class StuttershotEnchant extends WbsEnchantment implements ProjectileEnchant<Projectile> {
    private static final double CHANCE_PER_LEVEL = 10;

    private static final String DESCRIPTION = "A ranged weapon enchantment that gives a " + CHANCE_PER_LEVEL + "% " +
            "chance to shoot again immediately after firing!";

    public StuttershotEnchant() {
        super("stuttershot", DESCRIPTION);

        maxLevel = 3;
        supportedItems = WbsEnchantsBootstrap.ENCHANTABLE_PROJECTILE_WEAPON;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Stuttershot";
    }

    @Override
    public Class<Projectile> getProjectileClass() {
        return Projectile.class;
    }

    @Override
    public void onShoot(ProjectileLaunchEvent event, Projectile projectile, @NotNull ProjectileEnchant.WrappedProjectileSource source, int level) {
        if (WbsMath.chance(CHANCE_PER_LEVEL * level)) {
            Entity copy = projectile.copy();
            Location location = projectile.getLocation();

            new BukkitRunnable() {
                @Override
                public void run() {
                    Location spawnLoc = location;

                    if (source.livingShooter() != null && source.livingShooter().isValid()) {
                        spawnLoc = source.livingShooter().getEyeLocation();
                    }

                    copy.spawnAt(spawnLoc);
                }
            }.runTaskLater(WbsEnchants.getInstance(), 5);
        }
    }

    @Override
    public void onHit(ProjectileHitEvent event, Projectile projectile, @NotNull WrappedProjectileSource source, int level) {

    }
}
