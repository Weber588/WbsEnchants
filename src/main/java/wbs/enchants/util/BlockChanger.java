package wbs.enchants.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchants;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class BlockChanger {
    private static final Multimap<UUID, Block> PLAYER_BREAKING_INSTANCES = HashMultimap.create();

    public static boolean isPlayerBreaking(Player player, Block block) {
        return PLAYER_BREAKING_INSTANCES.get(player.getUniqueId()).contains(block);
    }

    @NotNull
    public static BlockChanger prepare(@NotNull List<Block> blocksToUpdate) {
        return new BlockChanger(blocksToUpdate);
    }

    private int delayTicks = 0;
    private int toUpdatePerChunk;
    @NotNull
    private final List<Block> blocksToUpdate;
    @NotNull
    private Predicate<Block> matching = check -> true;
    private Consumer<Block> beforeUpdate;
    private Consumer<Block> afterUpdate;

    public BlockChanger(@NotNull List<Block> blocksToUpdate) {
        this.blocksToUpdate = new LinkedList<>(blocksToUpdate);
        this.toUpdatePerChunk = blocksToUpdate.size();
    }

    public void breakBlocks(@NotNull Player player) {
        run(player, player::breakBlock);
    }

    public void run(@NotNull Player player, @NotNull Function<Block, Boolean> operation) {
        if (blocksToUpdate.isEmpty()) {
            return;
        }
        toUpdatePerChunk = Math.max(1, toUpdatePerChunk);

        PLAYER_BREAKING_INSTANCES.putAll(player.getUniqueId(), blocksToUpdate);

        List<Block> originalBlocks = new LinkedList<>(blocksToUpdate);

        ItemStack originalItem = player.getInventory().getItemInMainHand();

        int finalToUpdatePerChunk = toUpdatePerChunk;

        WbsEnchants plugin = WbsEnchants.getInstance();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < finalToUpdatePerChunk; i++) {
                    if (!updateNext()) {
                        cancel();
                        originalBlocks.forEach(block -> PLAYER_BREAKING_INSTANCES.remove(player.getUniqueId(), block));
                        return;
                    }
                }
            }

            /**
             * @return Whether or not a block was broken
             */
            private boolean updateNext() {
                boolean updated;
                do {
                    if (!player.isOnline()) {
                        return false;
                    }
                    ItemStack currentItem = player.getInventory().getItemInMainHand();

                    if (!currentItem.equals(originalItem)) {
                        return false;
                    }

                    ItemMeta meta = currentItem.getItemMeta();
                    if (meta != null) {
                        if (meta instanceof Damageable damageable) {
                            if (damageable.getDamage() >= currentItem.getType().getMaxDurability() - 1) {
                                return false;
                            }
                        }
                    }

                    Block toUpdate;
                    do {
                        if (blocksToUpdate.isEmpty()) {
                            return false;
                        }

                        toUpdate = blocksToUpdate.removeFirst();
                    } while (!matching.test(toUpdate));

                    updated = apply(toUpdate, operation);

                } while (!updated);

                return true;
            }
        }.runTaskTimer(plugin, delayTicks, delayTicks);
    }

    public boolean apply(@NotNull Block block, @NotNull Function<Block, Boolean> operation) {
        if (beforeUpdate != null) {
            beforeUpdate.accept(block);
        }

        boolean updated = operation.apply(block);

        if (updated) {
            if (afterUpdate != null) {
                afterUpdate.accept(block);
            }
        }

        return updated;
    }

    public BlockChanger setDelayTicks(int delayTicks) {
        this.delayTicks = delayTicks;
        return this;
    }

    public BlockChanger setToUpdatePerChunk(int toUpdatePerChunk) {
        this.toUpdatePerChunk = toUpdatePerChunk;
        return this;
    }

    public BlockChanger setMatching(@NotNull Predicate<Block> matching) {
        this.matching = matching;
        return this;
    }

    public BlockChanger beforeUpdate(Consumer<Block> beforeUpdate) {
        this.beforeUpdate = beforeUpdate;
        return this;
    }

    public BlockChanger afterUpdate(Consumer<Block> afterUpdate) {
        this.afterUpdate = afterUpdate;
        return this;
    }
}
