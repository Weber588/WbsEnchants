package wbs.enchants.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MaterialUtils {
    public static double getCompostChance(Material material) {
        double chance = switch (material) {
            case BEETROOT_SEEDS, BEETROOTS, KELP, KELP_PLANT, DRIED_KELP,
                    GLOW_BERRIES, CAVE_VINES, CAVE_VINES_PLANT, SHORT_GRASS,
                    HANGING_ROOTS, MANGROVE_PROPAGULE, MANGROVE_ROOTS,
                    MELON_SEEDS, MELON_STEM, MOSS_CARPET, PINK_PETALS, PITCHER_POD,
                    PITCHER_CROP, PUMPKIN_SEEDS, PUMPKIN_STEM, SEAGRASS, TALL_SEAGRASS,
                    SMALL_DRIPLEAF, SWEET_BERRIES, SWEET_BERRY_BUSH, TORCHFLOWER_SEEDS,
                    TORCHFLOWER_CROP, WHEAT_SEEDS
                    -> 30;
            case CACTUS, DRIED_KELP_BLOCK, FLOWERING_AZALEA_LEAVES, GLOW_LICHEN,
                    MELON_SLICE, NETHER_SPROUTS, SUGAR_CANE, TALL_GRASS, TWISTING_VINES,
                    TWISTING_VINES_PLANT, VINE, WEEPING_VINES, WEEPING_VINES_PLANT
                    -> 50;
            case APPLE, AZALEA, BEETROOT, BIG_DRIPLEAF, BIG_DRIPLEAF_STEM, CARROT,
                    CARROTS, COCOA_BEANS, FERN, LARGE_FERN, CRIMSON_FUNGUS, WARPED_FUNGUS,
                    LILY_PAD, MELON, MOSS_BLOCK, BROWN_MUSHROOM, RED_MUSHROOM,
                    NETHER_WART, POTATO, POTATOES, PUMPKIN, CARVED_PUMPKIN, CRIMSON_ROOTS,
                    WARPED_ROOTS, SEA_PICKLE, SHROOMLIGHT, SPORE_BLOSSOM, WHEAT
                    -> 65;
            case BAKED_POTATO, BREAD, COOKIE, FLOWERING_AZALEA, HAY_BLOCK,
                    BROWN_MUSHROOM_BLOCK, RED_MUSHROOM_BLOCK, NETHER_WART_BLOCK, PITCHER_PLANT,
                    TORCHFLOWER, WARPED_WART_BLOCK
                    -> 85;
            case CAKE, PUMPKIN_PIE
                -> 100;
            default -> 0;
        };

        if (chance == 0) {
            if (Tag.LEAVES.isTagged(material)) {
                return 30;
            }
            if (Tag.SAPLINGS.isTagged(material)) {
                return 30;
            }
            if (Tag.FLOWERS.isTagged(material)) {
                return 65;
            }
            if (Tag.CANDLE_CAKES.isTagged(material)) {
                return 100;
            }
        }
        return chance;
    }

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
