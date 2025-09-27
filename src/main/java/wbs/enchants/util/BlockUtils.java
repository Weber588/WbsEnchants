package wbs.enchants.util;

import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

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
}
