package wbs.enchants.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.block.CraftBlockType;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class BlockUtils {
    @NotNull
    public static BlockFace getClosestFace(@NotNull Vector facing, @NotNull Set<@NotNull BlockFace> options) {
        if (options.isEmpty()) {
            throw new IllegalArgumentException("Facing options cannot be empty");
        }

        BlockFace closest = BlockFace.UP;
        double smallestAngle = 180;
        for (BlockFace option : options) {
            double angle = facing.angle(option.getDirection());

            if (angle < smallestAngle) {
                smallestAngle = angle;
                closest = option;
            }
        }

        return closest;
    }

    public static Axis getClosestAxis(@NotNull Vector facing) {
        double x = facing.getX();
        double y = facing.getY();
        double z = facing.getZ();

        if (x >= y && x >= z) {
            return Axis.X;
        }
        if (y >= x && y >= z) {
            return Axis.Y;
        }

        return Axis.Z;
    }

    public static Axis axisFromFace(BlockFace face) {
        return switch (face) {
            case NORTH -> Axis.Z;
            case EAST -> Axis.X;
            case SOUTH -> Axis.Z;
            case WEST -> Axis.X;
            case UP -> Axis.Y;
            case DOWN -> Axis.Y;
            default -> throw new IllegalArgumentException("BlockFace %s is not axis aligned.".formatted(face.toString()));
        };
    }

    @Nullable
    public static Material getPlacementMaterial(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }
        Item nmsItemType = CraftItemType.bukkitToMinecraft(item.getType());
        Block nmsBlockType = Block.byItem(nmsItemType);

        if (nmsBlockType == Blocks.AIR) {
            return null;
        }

        return CraftBlockType.minecraftToBukkit(nmsBlockType);
    }
}
