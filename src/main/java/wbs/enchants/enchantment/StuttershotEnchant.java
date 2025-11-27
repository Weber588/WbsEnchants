package wbs.enchants.enchantment;

import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_PROJECTILE_WEAPON)
                .maxLevel(3);
    }

    @Override
    public Class<Projectile> getProjectileClass() {
        return Projectile.class;
    }

    @Override
    public void onShoot(ProjectileLaunchEvent event, Projectile projectile, @NotNull ProjectileEnchant.WrappedProjectileSource source, int level) {
        @Nullable LivingEntity livingShooter = source.livingShooter();
        if (livingShooter != null) {
            if (!livingShooter.isValid() || (livingShooter instanceof Player player && !player.isOnline())) {
                return;
            }

            if (projectile instanceof AbstractArrow arrow) {
                EntityEquipment equipment = livingShooter.getEquipment();
                if (equipment != null && !equipment.getItemInMainHand().equals(arrow.getWeapon()) && !equipment.getItemInOffHand().equals(arrow.getWeapon())) {
                    return;
                }
            }
        }

        if (WbsMath.chance(CHANCE_PER_LEVEL * level)) {
            Integer copyValue = projectile.getPersistentDataContainer().get(getKey(), PersistentDataType.INTEGER);

            if (copyValue != null && copyValue > 10) {
                return;
            }

            Projectile copy = (Projectile) projectile.copy();
            Location location = projectile.getLocation();

            copy.getPersistentDataContainer().set(getKey(), PersistentDataType.INTEGER, copyValue == null ? 1 : copyValue + 1);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Location spawnLoc = location;

                    if (livingShooter != null && livingShooter.isValid()) {
                        spawnLoc = livingShooter.getEyeLocation();
                    }

                    if (copy instanceof AbstractArrow arrow) {
                        arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
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
