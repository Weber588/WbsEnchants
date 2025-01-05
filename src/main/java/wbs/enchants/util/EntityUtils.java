package wbs.enchants.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.entities.WbsEntityUtil;

import java.util.HashMap;

public class EntityUtils {

    public static Tag<EntityType> getUndead() {
        return Bukkit.getTag("entity_types", NamespacedKey.minecraft("undead"), EntityType.class);
    }

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
            case SNOW_GOLEM, STRAY, POLAR_BEAR -> true;
            default -> false;
        };
    }

    public static void giveSafely(Player player, ItemStack item) {
        giveSafely(player.getInventory(), player.getLocation(), item);
    }

    public static void giveSafely(Inventory inventory, Location fallbackLocation, ItemStack item) {
        HashMap<Integer, ItemStack> failed = inventory.addItem(item);
        if (!failed.isEmpty()) {
            fallbackLocation.getWorld().dropItemNaturally(fallbackLocation, item);
        }
    }

    public static ItemStack getEnchantedFromSlot(LivingEntity entity, WbsEnchantment enchantment) {
        return getEnchantedFromSlot(entity, enchantment, EquipmentSlot.HAND);
    }

    @Nullable
    public static ItemStack getEnchantedFromSlot(LivingEntity entity, WbsEnchantment enchantment, EquipmentSlot slot) {
        return enchantment.getIfEnchanted(entity, slot);
    }
}
