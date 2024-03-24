package wbs.enchants.util;

import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchants;

import java.util.*;

@SuppressWarnings("unused")
public class MaterialUtils {
    private static final List<MaterialAgeSequence> COPPER_SEQUENCES = new LinkedList<>();

    private static final MaterialAgeSequence COPPER_BLOCKS = new MaterialAgeSequence(
            Material.COPPER_BLOCK, Material.EXPOSED_COPPER, Material.WEATHERED_COPPER, Material.OXIDIZED_COPPER
    );
    private static final MaterialAgeSequence CUT_COPPER = new MaterialAgeSequence(
            Material.CUT_COPPER, Material.EXPOSED_CUT_COPPER, Material.WEATHERED_CUT_COPPER,
            Material.OXIDIZED_CUT_COPPER
    );
    private static final MaterialAgeSequence CUT_COPPER_SLAB = new MaterialAgeSequence(
            Material.CUT_COPPER_SLAB, Material.EXPOSED_CUT_COPPER_SLAB,
            Material.WEATHERED_CUT_COPPER_SLAB, Material.OXIDIZED_CUT_COPPER_SLAB
    );
    private static final MaterialAgeSequence CUT_COPPER_STAIRS = new MaterialAgeSequence(
            Material.CUT_COPPER_STAIRS, Material.EXPOSED_CUT_COPPER_STAIRS,
            Material.WEATHERED_CUT_COPPER_STAIRS, Material.OXIDIZED_CUT_COPPER_STAIRS
    );

    private static final MaterialAgeSequence WAXED_COPPER_BLOCKS = new MaterialAgeSequence(
            Material.WAXED_COPPER_BLOCK, Material.WAXED_EXPOSED_COPPER, Material.WAXED_WEATHERED_COPPER, Material.WAXED_OXIDIZED_COPPER
    );
    private static final MaterialAgeSequence WAXED_CUT_COPPER = new MaterialAgeSequence(
            Material.WAXED_CUT_COPPER, Material.WAXED_EXPOSED_CUT_COPPER, Material.WAXED_WEATHERED_CUT_COPPER,
            Material.WAXED_OXIDIZED_CUT_COPPER
    );
    private static final MaterialAgeSequence WAXED_CUT_COPPER_SLAB = new MaterialAgeSequence(
            Material.WAXED_CUT_COPPER_SLAB, Material.WAXED_EXPOSED_CUT_COPPER_SLAB,
            Material.WAXED_WEATHERED_CUT_COPPER_SLAB, Material.WAXED_OXIDIZED_CUT_COPPER_SLAB
    );
    private static final MaterialAgeSequence WAXED_CUT_COPPER_STAIRS = new MaterialAgeSequence(
            Material.WAXED_CUT_COPPER_STAIRS, Material.WAXED_EXPOSED_CUT_COPPER_STAIRS,
            Material.WAXED_WEATHERED_CUT_COPPER_STAIRS, Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS
    );

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

    public static boolean isRepairMaterial(ItemStack stack, Material toCheck) {
        if (stack.getType() == toCheck) {
            return true;
        }

        return getRepairMaterials(stack).contains(toCheck);
    }

    // TODO: Make this configurable to move away from vanilla if desired?
    public static Set<Material> getRepairMaterials(ItemStack stack) {
        Set<Material> repairMaterials = new HashSet<>();
        repairMaterials.add(stack.getType());

        switch (stack.getType()) {
            case Tag
        }

        return repairMaterials;
    }

    public static boolean isAgedCopper(Material type) {
        return getCopperProgression(type) != null;
    }

    @Nullable
    public static MaterialAgeSequence getCopperProgression(Material type) {
        if (type == null) {
            return null;
        }
        return COPPER_SEQUENCES.stream()
                .filter(seq -> seq.contains(type))
                .findAny()
                .orElse(null);
    }

    private static class MaterialAgeSequence {
        private final List<Material> materials = new LinkedList<>();
        public MaterialAgeSequence(Material ... materials) {
            this.materials.addAll(Arrays.asList(materials));

            COPPER_SEQUENCES.add(this);
        }

        public boolean contains(Material material) {
            return materials.contains(material);
        }

        public Material get(int i) {
            return materials.get(i);
        }

        @Contract("!null -> !null; null -> null")
        public Material next(Material material) {
            return next(material, 1);
        }

        @Contract("!null, _ -> !null; null, _ -> null")
        public Material next(Material material, int amount) {
            int index = materials.indexOf(material);
            if (index == -1) {
                return material;
            }

            index += amount;

            if (index >= materials.size()) {
                return materials.get(materials.size() - 1);
            } else if (index < 0) {
                return materials.get(0);
            }

            return materials.get(index);
        }

        @Contract("!null -> !null; null-> null")
        public Material prev(Material material) {
            return prev(material, 1);
        }

        @Contract("!null, _ -> !null; null, _ -> null")
        public Material prev(Material material, int amount) {
            return next(material, -amount);
        }
    }
}
