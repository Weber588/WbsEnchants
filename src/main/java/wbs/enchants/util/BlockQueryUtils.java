package wbs.enchants.util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockQueryUtils {
    public static List<Block> getSquareMatching(@NotNull Block central, int halfWidth, BlockFace face, Predicate<Block> matching) {
        List<Block> found = new LinkedList<>();
        found.add(central);

        Queue<Block> searchQueue = new LinkedList<>();

        Block current = central;
        Predicate<Block> finalPredicate = check -> {
            if (found.contains(check) || !matching.test(check)) {
                return false;
            }

            int xDiff = Math.abs(check.getX() - central.getX());
            int yDiff = Math.abs(check.getY() - central.getY());
            int zDiff = Math.abs(check.getZ() - central.getZ());

            return xDiff <= halfWidth && yDiff <= halfWidth && zDiff <= halfWidth;
        };

        while (current != null) {
            List<Block> adjacent = getAdjacentInPlane(current, face, finalPredicate);

            found.addAll(adjacent);
            searchQueue.addAll(adjacent);

            current = searchQueue.poll();
        }

        found.remove(central);

        return found;
    }
    public static List<Block> getVeinMatching(@NotNull Block central, int blocksToGet, Predicate<Block> matching) {
        return getVeinMatching(central, blocksToGet, matching, false);
    }

    public static List<Block> getVeinMatching(@NotNull Block central, int blocksToGet, Predicate<Block> matching, boolean includeDiagonals) {
        List<Block> found = new LinkedList<>();
        found.add(central);

        Queue<Block> searchQueue = new LinkedList<>();

        Block current = central;

        Predicate<Block> finalPredicate = check ->
                !found.contains(check) && matching.test(check);

        // +1 to account for the first element being the central block
        while (found.size() < blocksToGet + 1 && current != null) {
            List<Block> adjacent = getAdjacentBlocks(current, finalPredicate, includeDiagonals);

            found.addAll(adjacent);
            searchQueue.addAll(adjacent);

            current = searchQueue.poll();
        }

        found.remove(central);

        if (found.size() > blocksToGet) {
            return found.subList(0, blocksToGet - 1);
        }
        return found;
    }

    public static List<Block> getAdjacentBlocks(@NotNull Block central, Predicate<Block> matching) {
        return getAdjacentBlocks(central, matching, false);
    }

    public static List<Block> getAdjacentBlocks(@NotNull Block central, Predicate<Block> matching, boolean includeDiagonals) {
        int centralX = central.getX();
        int centralY = central.getY();
        int centralZ = central.getZ();

        World world = central.getWorld();

        List<Block> blocks = new LinkedList<>(List.of(
                world.getBlockAt(centralX + 1, centralY, centralZ),
                world.getBlockAt(centralX - 1, centralY, centralZ),
                world.getBlockAt(centralX, centralY + 1, centralZ),
                world.getBlockAt(centralX, centralY - 1, centralZ),
                world.getBlockAt(centralX, centralY, centralZ + 1),
                world.getBlockAt(centralX, centralY, centralZ - 1)
        ));

        if (includeDiagonals) {
            blocks.addAll(List.of(
                    // Edge diagonals
                    world.getBlockAt(centralX + 1, centralY + 1, centralZ),
                    world.getBlockAt(centralX - 1, centralY + 1, centralZ),
                    world.getBlockAt(centralX, centralY + 1, centralZ + 1),
                    world.getBlockAt(centralX, centralY + 1, centralZ - 1),

                    world.getBlockAt(centralX + 1, centralY - 1, centralZ),
                    world.getBlockAt(centralX - 1, centralY - 1, centralZ),
                    world.getBlockAt(centralX, centralY - 1, centralZ + 1),
                    world.getBlockAt(centralX, centralY - 1, centralZ - 1),

                    // Corner diagonals
                    world.getBlockAt(centralX + 1, centralY - 1, centralZ + 1),
                    world.getBlockAt(centralX - 1, centralY - 1, centralZ + 1),
                    world.getBlockAt(centralX + 1, centralY - 1, centralZ + 1),
                    world.getBlockAt(centralX + 1, centralY - 1, centralZ - 1),

                    world.getBlockAt(centralX + 1, centralY - 1, centralZ + 1),
                    world.getBlockAt(centralX - 1, centralY - 1, centralZ + 1),
                    world.getBlockAt(centralX + 1, centralY - 1, centralZ + 1),
                    world.getBlockAt(centralX + 1, centralY - 1, centralZ - 1)
            ));
        }

        return blocks.stream()
                .filter(matching)
                .collect(Collectors.toList());
    }


    public static List<Block> getAdjacentInPlane(@NotNull Block central, BlockFace plane, Predicate<Block> matching) {
        int centralX = central.getX();
        int centralY = central.getY();
        int centralZ = central.getZ();

        World world = central.getWorld();

        boolean doX = false;
        boolean doY = false;
        boolean doZ = false;

        switch (plane) {
            case NORTH, SOUTH -> {
                doX = true;
                doY = true;
            }
            case EAST, WEST -> {
                doY = true;
                doZ = true;
            }
            case UP, DOWN -> {
                doX = true;
                doZ = true;
            }
            default -> throw new IllegalArgumentException("Only cardinal block faces are supported.");
        }

        List<Block> found = new LinkedList<>();

        if (doX) {
            found.add(world.getBlockAt(centralX + 1, centralY, centralZ));
            found.add(world.getBlockAt(centralX - 1, centralY, centralZ));
        }
        if (doY) {
            found.add(world.getBlockAt(centralX, centralY + 1, centralZ));
            found.add(world.getBlockAt(centralX, centralY - 1, centralZ));
        }
        if (doZ) {
            found.add(world.getBlockAt(centralX, centralY, centralZ + 1));
            found.add(world.getBlockAt(centralX, centralY, centralZ - 1));
        }

        return found.stream()
                .filter(matching)
                .collect(Collectors.toList());
    }

    public static List<Block> getAdjacent(@NotNull List<Block> surrounding, Predicate<Block> matching) {
        List<Block> allAdjacent = new LinkedList<>();
        for (Block block : surrounding) {
            List<Block> adjacent = getAdjacentBlocks(block, matching);

            adjacent.forEach(toAdd -> {
                if (!allAdjacent.contains(toAdd)) {
                    allAdjacent.add(toAdd);
                }
            });
        }

        return allAdjacent;
    }
}
