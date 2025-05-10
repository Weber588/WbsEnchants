package wbs.enchants.enchantment;

import org.bukkit.entity.*;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.VehicleEnchant;
import wbs.enchants.util.EntityUtils;

public class QuickRideEnchant extends WbsEnchantment implements VehicleEnchant {
    private static final String DEFAULT_DESCRIPTION = "Automatically get into the enchanted vehicle when placed, " +
            "and automatically return it to your inventory upon exiting.";

    public QuickRideEnchant() {
        super("quick_ride", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_VEHICLE);
    }

    @Override
    public void afterPlace(EntityPlaceEvent event, ItemStack placedItem) {
        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        event.getEntity().addPassenger(player);
    }

    @EventHandler
    public void onExitVehicle(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            Entity dismounted = event.getDismounted();

            if (isEnchanted(dismounted) && dismounted instanceof Damageable damageable) {
                player.damage(damageable.getHealth() + 1, dismounted);
            }
        }
    }

    @Override
    public void afterDrop(VehicleDestroyEvent event, Item spawnedItem) {
        if (event.getAttacker() instanceof Player player) {
            spawnedItem.remove();
            EntityUtils.giveSafely(player, spawnedItem.getItemStack());
        }
    }

    @Override
    public boolean canEnchant(Entity entity) {
        return entity instanceof Boat || entity instanceof RideableMinecart;
    }
}
