package wbs.enchants.util;

import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.entities.WbsEntityUtil;

import java.util.HashMap;

public class EntityUtils {
    public static boolean willCrit(LivingEntity entity) {
        return entity.getFallDistance() > 0 &&
                !entity.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                entity.getVehicle() == null &&
                !Tag.FALL_DAMAGE_RESETTING.isTagged(entity.getLocation().getBlock().getType()) &&
                !WbsEntityUtil.isInWater(entity)
        ;
    }
    
    public static boolean isColdVulnerable(EntityType type) {
        return switch(type) {
            case ENDERMAN, BLAZE, STRIDER, MAGMA_CUBE, HUSK -> true;
            default -> false;
        };
    }

    public static boolean isHotVulnerable(EntityType type) {
        return switch(type) {
            case SNOWMAN, STRAY, POLAR_BEAR -> true;
            default -> false;
        };
    }

    public static void giveSafely(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> failed = player.getInventory().addItem(item);
        if (!failed.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    /**
     * Checks if the given entity has an item enchanted with the given enchantment in the given slot, returning either
     * the item from that slot containing that enchantment, or null if it did not meet those conditions.
     * @param entity The entity whose {@link org.bukkit.inventory.EntityEquipment} to check.
     * @param enchantment The enchantment to verify is on the item before returning it.
     * @param slot The slot to check for the enchanted item.
     * @return An item from the given slot of the given entity, enchanted with the given enchantment, or null.
     */
    @Nullable
    public static ItemStack getEnchantedFromSlot(LivingEntity entity, WbsEnchantment enchantment, EquipmentSlot slot) {
        if (slot == null) {
            return null;
        }

        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return null;
        }

        ItemStack item = equipment.getItem(slot);
        if (enchantment.containsEnchantment(item)) {
            return item;
        }

        return null;
    }
}
