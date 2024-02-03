package wbs.enchants.enchantment.helper;

import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.EventUtils;

public interface VehicleEnchant {
    default void registerVehicleEvents() {
        EventUtils.register(EntityPlaceEvent.class, this::onPlace);
        EventUtils.register(VehicleDestroyEvent.class, this::onBreak);
    }

    default void onPlace(EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Vehicle)) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return;
        }

        Entity entity = event.getEntity();

        if (!canEnchant(entity)) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        ItemStack placedItem = equipment.getItem(event.getHand());
        if (enchant.containsEnchantment(placedItem)) {
            int level = enchant.getLevel(placedItem);

            PersistentDataContainer container = entity.getPersistentDataContainer();
            container.set(key, PersistentDataType.INTEGER, level);

            afterPlace(event, placedItem);
        }
    }

    default void onBreak(VehicleDestroyEvent event) {
        if (event.getAttacker() instanceof Player player && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Vehicle vehicle = event.getVehicle();
        PersistentDataContainer entityContainer = vehicle.getPersistentDataContainer();
        WbsEnchantment enchant = getThisEnchantment();

        Material material;
        if (vehicle instanceof Boat boat) {
            material = boat.getBoatType().getMaterial();
        } else if (vehicle instanceof Minecart minecart) {
            material = switch (minecart.getType()) {
                case MINECART -> Material.MINECART;
                case MINECART_CHEST -> Material.CHEST_MINECART;
                case MINECART_FURNACE -> Material.FURNACE_MINECART;
                case MINECART_HOPPER -> Material.HOPPER_MINECART;
                case MINECART_TNT-> Material.TNT_MINECART;
                default -> null;
            };
        } else {
            return;
        }

        if (material == null) {
            return;
        }

        ItemStack item = new ItemStack(material);

        if (!enchant.canEnchantItem(item)) {
            return;
        }

        NamespacedKey key = enchant.getKey();

        Integer level = entityContainer.get(key, PersistentDataType.INTEGER);
        if (level != null) {
            event.setCancelled(true);

            vehicle.remove();
            UberUtils.addEnchantment(enchant, item, level);
            vehicle.getWorld().dropItemNaturally(vehicle.getLocation(), item);
            afterDrop(event, item);
        }
    }

    default void afterPlace(EntityPlaceEvent event, ItemStack placedItem) {

    }

    default void afterDrop(VehicleDestroyEvent event, ItemStack droppedItem) {

    }

    WbsEnchantment getThisEnchantment();
    boolean canEnchant(Entity entity);
}
