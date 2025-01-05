package wbs.enchants.enchantment;

import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ProjectileEnchant;
import wbs.enchants.util.EntityUtils;

public class EchoingEnchant extends WbsEnchantment implements ProjectileEnchant<AbstractArrow> {
    private static final String DESCRIPTION = "When a projectile hits a block, if it can be picked up, it'll " +
            "immediately return to your inventory!";

    public EchoingEnchant() {
        super("echoing", DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_PROJECTILE_WEAPON);
    }

    @Override
    public Class<AbstractArrow> getProjectileClass() {
        return AbstractArrow.class;
    }

    @Override
    public void onShoot(ProjectileLaunchEvent event, AbstractArrow projectile, @NotNull WrappedProjectileSource source, int level) {

    }

    @Override
    public void onHit(ProjectileHitEvent event, AbstractArrow projectile, @NotNull WrappedProjectileSource source, int level) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (projectile.isValid() && projectile.isInBlock()) {
                    projectile.remove();

                    Inventory shooterInventory = null;
                    Location fallbackLocation = null;
                    if (source.livingShooter() != null && source.livingShooter() instanceof InventoryHolder holder) {
                        shooterInventory = holder.getInventory();
                        fallbackLocation = source.livingShooter().getLocation();
                    } else if (source.blockShooter() != null && source.blockShooter() instanceof InventoryHolder holder) {
                        shooterInventory = holder.getInventory();
                        fallbackLocation = source.blockShooter().getLocation();
                    }

                    if (shooterInventory != null) {
                        EntityUtils.giveSafely(shooterInventory, fallbackLocation, source.projectileItem());
                    }
                }
            }
        }.runTaskLater(WbsEnchants.getInstance(), 1);
    }
}
