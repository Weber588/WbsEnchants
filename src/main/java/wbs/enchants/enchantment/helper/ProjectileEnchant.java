package wbs.enchants.enchantment.helper;

import org.bukkit.block.BlockState;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EventUtils;

import java.util.Objects;

public interface ProjectileEnchant extends BlockEnchant {

    default void registerProjectileEvents() {
        EventUtils.register(ProjectileLaunchEvent.class, this::onShoot, EventPriority.NORMAL, true);
    }


    default void onShoot(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();

        ProjectileSource shooter = projectile.getShooter();

        WbsEnchantment thisEnchant = getThisEnchantment();

        BlockState blockShooter = null;
        LivingEntity livingShooter = null;

        if (shooter instanceof BlockState) {
            blockShooter = (BlockState) shooter;
        } else if (shooter instanceof LivingEntity) {
            livingShooter = (LivingEntity) shooter;
        }

        ItemStack projectileItem = null;
        ItemStack shooterWeapon = null;

        if (projectile instanceof AbstractArrow arrow) {
            shooterWeapon = arrow.getWeapon();
            projectileItem = arrow.getItemStack();
        } else if (projectile instanceof ThrowableProjectile thrown) {
            projectileItem = thrown.getItem();
        }

        int level = -1;
        if (shooterWeapon != null && thisEnchant.isEnchantmentOn(shooterWeapon)) {
            level = thisEnchant.getLevel(shooterWeapon);
        } else if (projectileItem != null && thisEnchant.isEnchantmentOn(projectileItem)) {
            level = thisEnchant.getLevel(projectileItem);
        } else if (blockShooter != null) {
            level = Objects.requireNonNullElse(getLevel(blockShooter.getBlock()), level);
        } else {
            return;
        }

        onShoot(projectile, livingShooter, blockShooter, projectileItem, shooterWeapon, level);
    }

    void onShoot(Projectile projectile,
                 @Nullable LivingEntity shooter,
                 @Nullable BlockState blockShooter,
                 @Nullable ItemStack projectileItem,
                 @Nullable ItemStack shootingItem,
                 int level);
}
