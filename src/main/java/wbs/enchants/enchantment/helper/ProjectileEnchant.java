package wbs.enchants.enchantment.helper;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EventUtils;

import java.util.Objects;

public interface ProjectileEnchant<T extends Projectile> extends BlockEnchant {

    default void registerProjectileEvents() {
        EventUtils.register(ProjectileLaunchEvent.class, this::onShoot, EventPriority.NORMAL, true);
        EventUtils.register(ProjectileHitEvent.class, this::onHit, EventPriority.NORMAL, true);
    }

    private static WrappedProjectileSource getSource(Projectile projectile) {
        ProjectileSource shooter = projectile.getShooter();

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

        return new WrappedProjectileSource(livingShooter, blockShooter, projectileItem, shooterWeapon);
    }

    default Integer getLevel(WrappedProjectileSource source) {
        WbsEnchantment thisEnchant = getThisEnchantment();

        Integer level = null;

        if (source.shootingItem != null && thisEnchant.isEnchantmentOn(source.shootingItem)) {
            level = thisEnchant.getLevel(source.shootingItem);
        } else if (source.projectileItem != null && thisEnchant.isEnchantmentOn(source.projectileItem)) {
            level = thisEnchant.getLevel(source.projectileItem);
        } else if (source.blockShooter != null) {
            level = Objects.requireNonNullElse(getLevel(source.blockShooter.getBlock()), level);
        }

        return level;
    }

    @Nullable
    default T castIfSubtype(Projectile projectile) {
        Class<T> projectileClass = getProjectileClass();

        if (projectileClass.isInstance(projectile)) {
            return projectileClass.cast(projectile);
        }
        return null;
    }

    default void onHit(ProjectileHitEvent event) {
        T projectile = castIfSubtype(event.getEntity());

        if (projectile == null) {
            return;
        }

        WrappedProjectileSource source = getSource(projectile);
        Integer level = getLevel(source);

        if (level == null) {
            return;
        }

        onHit(event, projectile, source, level);
    }

    default void onShoot(ProjectileLaunchEvent event) {
        T projectile = castIfSubtype(event.getEntity());

        if (projectile == null) {
            return;
        }

        WrappedProjectileSource source = getSource(projectile);
        Integer level = getLevel(source);

        if (level == null) {
            return;
        }

        onShoot(event, projectile, source, level);
    }

    Class<T> getProjectileClass();
    void onShoot(ProjectileLaunchEvent event, T projectile, @NotNull WrappedProjectileSource source, int level);
    void onHit(ProjectileHitEvent event, T projectile, @NotNull WrappedProjectileSource source, int level);

    record WrappedProjectileSource(@Nullable LivingEntity livingShooter,
                                   @Nullable BlockState blockShooter,
                                   @Nullable ItemStack projectileItem,
                                   @Nullable ItemStack shootingItem) {}

    @Override
    default boolean canEnchant(Block block) {
        return block.getState() instanceof ProjectileSource;
    }
}
