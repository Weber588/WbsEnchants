package wbs.enchants.enchantment.helper;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EnchantUtils;
import wbs.enchants.util.EventUtils;

public interface VehicleEnchant extends EntityEnchant {
    default void registerVehicleEvents() {
        EventUtils.register(VehicleDestroyEvent.class, this::onBreak);
    }

    default void onBreak(VehicleDestroyEvent event) {
        if (event.getAttacker() instanceof Player player && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Vehicle vehicle = event.getVehicle();

        Material material = EntityEnchant.getEntityMaterial(vehicle);

        if (material == null) {
            return;
        }

        ItemStack item = new ItemStack(material);

        WbsEnchantment enchant = getThisEnchantment();
        if (!enchant.getEnchantment().canEnchantItem(item)) {
            return;
        }

        NamespacedKey key = enchant.getKey();

        PersistentDataContainer entityContainer = vehicle.getPersistentDataContainer();

        Integer level = entityContainer.get(key, PersistentDataType.INTEGER);
        if (level != null) {
            event.setCancelled(true);

            vehicle.remove();
            EnchantUtils.addEnchantment(enchant, item, level);
            afterDrop(event, vehicle.getWorld().dropItemNaturally(vehicle.getLocation(), item));
        }
    }

    default void afterDrop(VehicleDestroyEvent event, Item spawnedItem) {

    }
}
