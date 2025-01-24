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
import wbs.utils.util.WbsEnums;

public interface VehicleEnchant extends EntityEnchant {
    default void registerVehicleEvents() {
        EventUtils.register(VehicleDestroyEvent.class, this::onBreak);
    }

    default void onBreak(VehicleDestroyEvent event) {
        if (event.getAttacker() instanceof Player player && player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        Vehicle vehicle = event.getVehicle();

        Material material;
        if (vehicle instanceof Boat boat) {
            if (boat instanceof ChestBoat) {
                material = switch (boat.getBoatType()) {
                    case OAK -> Material.OAK_CHEST_BOAT;
                    case SPRUCE -> Material.SPRUCE_CHEST_BOAT;
                    case BIRCH -> Material.BIRCH_CHEST_BOAT;
                    case JUNGLE -> Material.JUNGLE_CHEST_BOAT;
                    case ACACIA -> Material.ACACIA_CHEST_BOAT;
                    case CHERRY -> Material.CHERRY_CHEST_BOAT;
                    case DARK_OAK -> Material.DARK_OAK_CHEST_BOAT;
                    case MANGROVE -> Material.MANGROVE_CHEST_BOAT;
                    case BAMBOO -> Material.BAMBOO_CHEST_RAFT;
                };
            } else {
                material = switch (boat.getBoatType()) {
                    case OAK -> Material.OAK_BOAT;
                    case SPRUCE -> Material.SPRUCE_BOAT;
                    case BIRCH -> Material.BIRCH_BOAT;
                    case JUNGLE -> Material.JUNGLE_BOAT;
                    case ACACIA -> Material.ACACIA_BOAT;
                    case CHERRY -> Material.CHERRY_BOAT;
                    case DARK_OAK -> Material.DARK_OAK_BOAT;
                    case MANGROVE -> Material.MANGROVE_BOAT;
                    case BAMBOO -> Material.BAMBOO_RAFT;
                };
            }

            // Somewhat hacky future-proof in case more boats get added, try getting them by name.
            // Unsure why Boat.Type#getMaterial() doesn't return the type of boat, but rather the plank/crafting
            // version, but probably some legacy thing.
            //noinspection ConstantConditions
            if (material == null) {
                String checkString = boat.getBoatType().toString();
                if (boat instanceof ChestBoat) {
                    checkString += "_CHEST";
                }
                String boatCheck = checkString + "_BOAT";
                String raftCheck = checkString + "_RAFT";

                material = WbsEnums.getEnumFromString(Material.class, boatCheck);
                if (material == null) {
                    material = WbsEnums.getEnumFromString(Material.class, raftCheck);
                }
            }
        } else if (vehicle instanceof Minecart minecart) {
            material = switch (minecart.getType()) {
                case MINECART -> Material.MINECART;
                case CHEST_MINECART -> Material.CHEST_MINECART;
                case FURNACE_MINECART -> Material.FURNACE_MINECART;
                case HOPPER_MINECART -> Material.HOPPER_MINECART;
                case TNT_MINECART-> Material.TNT_MINECART;
                default -> null;
            };
        } else {
            return;
        }

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
            vehicle.getWorld().dropItemNaturally(vehicle.getLocation(), item);
            afterDrop(event, item);
        }
    }

    default void afterDrop(VehicleDestroyEvent event, ItemStack droppedItem) {

    }
}
