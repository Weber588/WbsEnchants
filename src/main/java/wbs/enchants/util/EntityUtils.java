package wbs.enchants.util;

import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import wbs.utils.util.entities.WbsEntityUtil;

import java.util.HashMap;

public class EntityUtils {
    public static boolean willCrit(Player player) {
        return player.getFallDistance() > 0 &&
                !player.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                player.getVehicle() == null &&
                !Tag.FALL_DAMAGE_RESETTING.isTagged(player.getLocation().getBlock().getType()) &&
                !WbsEntityUtil.isInWater(player)
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
}
