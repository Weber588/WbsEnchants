package wbs.enchants.util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class BlockQuery {
    @NotNull
    private Predicate<Block> blockPredicate = block -> true;
    @NotNull
    private DiagonalMode diagonalMode = DiagonalMode.NONE;
    private int maxDistance = -1;
    private int maxBlocks = -1; // Default to size of 1 chunk -- should be more than enough
    private DistanceMode distanceMode = DistanceMode.REAL;

    public BlockQuery() {}

    public BlockQuery(@NotNull Predicate<Block> blockPredicate, @NotNull DiagonalMode diagonalMode, int maxDistance, int maxBlocks, DistanceMode distanceMode) {
        this.blockPredicate = blockPredicate;
        this.diagonalMode = diagonalMode;
        this.maxDistance = maxDistance;
        this.maxBlocks = maxBlocks;
        this.distanceMode = distanceMode;
    }

    public BlockQuery setPredicate(@NotNull Predicate<Block> blockPredicate) {
        this.blockPredicate = blockPredicate;
        return this;
    }

    public BlockQuery setDiagonalMode(@NotNull DiagonalMode diagonalMode) {
        this.diagonalMode = diagonalMode;
        return this;
    }

    public BlockQuery setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
        return this;
    }

    public BlockQuery setMaxBlocks(int maxBlocks) {
        this.maxBlocks = maxBlocks;
        return this;
    }

    public BlockQuery setDistanceMode(DistanceMode distanceMode) {
        this.distanceMode = distanceMode;
        return this;
    }

    private List<Block> filter(List<Block> found) {
        if (found.size() > maxBlocks) {
            found = found.subList(0, maxBlocks - 1);
        }

        return found.stream().filter(blockPredicate).toList();
    }

    public List<Block> getSquare(@NotNull Block central, BlockFace face) {
        if (maxDistance <= 0) {
            throw new IllegalArgumentException("This operation requires a maximum distance.");
        }
        List<Block> found = new LinkedList<>();
        found.add(central);

        Queue<Block> searchQueue = new LinkedList<>();

        Block current = central;
        Predicate<Block> finalPredicate = check -> {
            if (found.contains(check) || !blockPredicate.test(check)) {
                return false;
            }

            int xDiff = Math.abs(check.getX() - central.getX());
            int yDiff = Math.abs(check.getY() - central.getY());
            int zDiff = Math.abs(check.getZ() - central.getZ());

            return xDiff <= maxDistance && yDiff <= maxDistance && zDiff <= maxDistance;
        };

        BlockQuery adjacentQuery = this.clone()
                .setPredicate(finalPredicate);

        while (current != null) {
            List<Block> adjacent = adjacentQuery.getAdjacentInPlane(current, face);

            found.addAll(adjacent);
            searchQueue.addAll(adjacent);

            current = searchQueue.poll();
        }

        found.remove(central);

        return filter(found);
    }

    public List<Block> getVein(@NotNull Block central) {
        List<Block> found = new LinkedList<>();
        found.add(central);

        Queue<Block> searchQueue = new LinkedList<>();

        Block current = central;

        Predicate<Block> finalPredicate = check ->
                !found.contains(check) && blockPredicate.test(check);

        BlockQuery adjacentQuery = this.clone()
                .setPredicate(finalPredicate);

        // +1 to account for the first element being the central block
        while (((maxBlocks > 0) && found.size() < maxBlocks + 1) && current != null) {
            List<Block> adjacent = adjacentQuery.getAdjacentBlocks(current)
                    .stream()
                    .filter(adjacentBlock -> isInDistance(central, adjacentBlock))
                    .toList();

            found.addAll(adjacent);
            searchQueue.addAll(adjacent);

            current = searchQueue.poll();
        }

        found.remove(central);

        return filter(found);
    }

    public List<Block> getNearby(Block central) {
        if (maxDistance <= 0) {
            throw new IllegalArgumentException("This operation requires a maximum distance.");
        }

        List<Block> nearby = new LinkedList<>();

        for (int x = 0; x < maxDistance; x++) {
            for (int y = 0; y < maxDistance; y++) {
                for (int z = 0; z < maxDistance; z++) {
                    Block check = central.getWorld().getBlockAt(x, y, z);

                    if (isInDistance(central, check)) {
                        nearby.add(check);
                    }
                }
            }
        }

        nearby.sort(Comparator.comparingInt(block1 -> (int) distanceMode.getDistance(central, block1)));

        return nearby;
    }

    public List<Block> getAdjacentBlocks(@NotNull Block central) {
        return getAdjacentBlocks(central, blockPredicate);
    }
    public List<Block> getAdjacentBlocks(@NotNull Block central, @NotNull Predicate<Block> blockPredicate) {
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

        if (diagonalMode == DiagonalMode.EDGE || diagonalMode == DiagonalMode.ALL) {
            blocks.addAll(List.of(
                    // Edge diagonals
                    world.getBlockAt(centralX + 1, centralY + 1, centralZ),
                    world.getBlockAt(centralX - 1, centralY + 1, centralZ),
                    world.getBlockAt(centralX, centralY + 1, centralZ + 1),
                    world.getBlockAt(centralX, centralY + 1, centralZ - 1),

                    world.getBlockAt(centralX + 1, centralY - 1, centralZ),
                    world.getBlockAt(centralX - 1, centralY - 1, centralZ),
                    world.getBlockAt(centralX, centralY - 1, centralZ + 1),
                    world.getBlockAt(centralX, centralY - 1, centralZ - 1)
            ));
        }

        if (diagonalMode == DiagonalMode.CORNER || diagonalMode == DiagonalMode.ALL) {
            blocks.addAll(List.of(
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

        return filter(blocks).stream().filter(blockPredicate).toList();
    }

    public List<Block> getAdjacentInPlane(@NotNull Block central, BlockFace plane) {
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

        // TODO: Make this respect diagonal mode
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

        return filter(found);
    }

    public List<Block> getAdjacent(@NotNull List<Block> toSurround) {
        return getAdjacent(toSurround, blockPredicate);
    }
    public List<Block> getAdjacent(@NotNull List<Block> toSurround, Predicate<Block> blockPredicate) {
        List<Block> allAdjacent = new LinkedList<>();
        for (Block block : toSurround) {
            List<Block> adjacent = getAdjacentBlocks(block, blockPredicate);

            adjacent.forEach(toAdd -> {
                if (!allAdjacent.contains(toAdd)) {
                    allAdjacent.add(toAdd);
                }
            });
        }

        return allAdjacent;
    }

    public boolean isInDistance(Block central, Block check) {
        return blockPredicate.test(check) && distanceMode.getDistance(central, check) <= maxDistance;
    }

    @Override
    public BlockQuery clone() {
        return new BlockQuery(blockPredicate, diagonalMode, maxDistance, maxBlocks, distanceMode);
    }

    public enum DiagonalMode {
        NONE,
        EDGE,
        CORNER,
        ALL
    }

    public enum DistanceMode {
        REAL((block1, block2) -> block1.getLocation().distance(block2.getLocation())),
        MANHATTAN(((block1, block2) -> (double) BlockQueryUtil.getManhattanDistance(block1, block2)))
        ;

        private final BiFunction<Block, Block, Double> getDistance;

        DistanceMode(BiFunction<Block, Block, Double> getDistance) {
            this.getDistance = getDistance;
        }

        public double getDistance(Block block1, Block block2) {
            return getDistance.apply(block1, block2);
        }
    }
}
