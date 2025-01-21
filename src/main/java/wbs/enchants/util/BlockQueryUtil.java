package wbs.enchants.util;

import org.bukkit.block.Block;

public class BlockQueryUtil {
    public static int getManhattanDistance(Block block1, Block block2) {
        int xDiff = Math.abs(block1.getX() - block2.getX());
        int yDiff = Math.abs(block1.getY() - block2.getY());
        int zDiff = Math.abs(block1.getZ() - block2.getZ());

        return xDiff + yDiff + zDiff;
    }
}
