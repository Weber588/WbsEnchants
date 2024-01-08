package wbs.enchants.util;

import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import wbs.utils.util.entities.WbsEntityUtil;

public class EntityUtils {
    public static boolean willCrit(Player player) {
        return player.getFallDistance() > 0 &&
                !player.hasPotionEffect(PotionEffectType.BLINDNESS) &&
                player.getVehicle() == null &&
                !Tag.FALL_DAMAGE_RESETTING.isTagged(player.getLocation().getBlock().getType()) &&
                !WbsEntityUtil.isInWater(player)
        ;
    }
}
