package wbs.enchants.enchantment;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ProjectileEnchant;
import wbs.utils.util.WbsMath;

public class StuttershotEnchant extends WbsEnchantment implements ProjectileEnchant {
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
    public boolean canEnchant(Block block) {
        return block.getState() instanceof ProjectileSource;
    }

    @Override
    public void onShoot(Projectile projectile, @Nullable LivingEntity livingShooter, @Nullable BlockState blockShooter, @Nullable ItemStack projectileItem, @Nullable ItemStack shootingItem, int level) {
        if (WbsMath.chance(CHANCE_PER_LEVEL * level)) {
            Entity copy = projectile.copy();
            Location location = projectile.getLocation();

            new BukkitRunnable() {
                @Override
                public void run() {
                    Location spawnLoc = location;

                    if (livingShooter != null && livingShooter.isValid()) {
                        spawnLoc = livingShooter.getEyeLocation();
                    }

                    copy.spawnAt(spawnLoc);
                }
            }.runTaskLater(WbsEnchants.getInstance(), 5);
        }
    }
}
