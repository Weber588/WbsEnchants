package wbs.enchants.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MaterialUtils {
    public static boolean isOre(@NotNull Block block) {
        return isOre(block.getType());
    }

    public static boolean isOre(@NotNull Material material) {
        List<Tag<Material>> oreTags = List.of(
                Tag.COAL_ORES,
                Tag.COPPER_ORES,
                Tag.IRON_ORES,
                Tag.GOLD_ORES,
                Tag.LAPIS_ORES,
                Tag.REDSTONE_ORES,
                Tag.EMERALD_ORES,
                Tag.DIAMOND_ORES
        );

        boolean isOreTagged = oreTags.stream().anyMatch(tag -> tag.isTagged(material));

        if (isOreTagged) {
            return true;
        }

        // No mutual tag for Quartz ore
        return material == Material.NETHER_QUARTZ_ORE;
    }
}
