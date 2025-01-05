package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.AbstractMultiBreakEnchant;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.BlockQueryUtils;
import wbs.utils.util.WbsMath;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class TreeFellerEnchant extends AbstractMultiBreakEnchant {

    private static final String DEFAULT_DESCRIPTION = "When breaking a log, the entire tree will be cut down, but " +
            "use up hunger in exchange for each block broken.";
    private static final double MAX_FOOD_LOSS_CHANCE = 50;

    public TreeFellerEnchant() {
        super("tree_feller", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeTagKeys.AXES);
    }

    @Override
    protected boolean canBreak(Block block) {
        return Tag.LOGS.isTagged(block.getType()) || Tag.LEAVES.isTagged(block.getType());
    }

    @Override
    protected void handleBreak(@NotNull BlockBreakEvent event, @NotNull Block broken, @NotNull Player player, @NotNull ItemStack item, int level) {
        int logsToBreak = (int) Math.pow(level + 1, 4); // 1 -> 16, 2 -> 81, 3 -> 256

        Material type = broken.getType();
        Predicate<Block> isSameType = check -> check.getType() == type;
        Predicate<Block> isNaturalLeaves = check -> {
            if (check.getBlockData() instanceof Leaves leaves) {
                return !leaves.isPersistent();
            }
            return false;
        };

        // Get amount + 1 -- if all are used, then there's too many to break.
        final List<Block> logs = BlockQueryUtils.getVeinMatching(broken, logsToBreak + 1, isSameType, true);

        if (logs.isEmpty()) {
            return;
        }

        // All leaves here will by definition, have distance minimum distance from a log
        final List<Block>  initialLeaves = BlockQueryUtils.getAdjacent(logs, isNaturalLeaves);

        if (initialLeaves.isEmpty()) {
            // Not a tree
            return;
        }

        if (logs.size() > logsToBreak) {
            sendActionBar("&cThe tree is too big to fell!", player);
            return;
        }

        List<Block> blocksToBreak = new LinkedList<>(logs);

        List<Block> currentLeaves = initialLeaves;
        while (!currentLeaves.isEmpty()) {
            Block first = currentLeaves.getFirst();
            int currentDistance = ((Leaves) first.getBlockData()).getDistance();
            blocksToBreak.addAll(currentLeaves);

            // Get Leaves that are exactly 1 block further from the logs than the previous layer
            currentLeaves = BlockQueryUtils.getAdjacent(currentLeaves, check -> {
                if (!isNaturalLeaves.test(check)) {
                    return false;
                }

                return (check.getBlockData() instanceof Leaves leaves) && (leaves.getDistance() == (currentDistance + 1));
            });
        }

        BlockChanger.prepare(blocksToBreak)
                .setDelayTicks(1)
                .setToUpdatePerChunk(level * 3)
                .setMatching(isSameType.or(isNaturalLeaves))
                .afterUpdate(alreadyBroken -> {
                    if (Tag.LEAVES.isTagged(alreadyBroken.getType()) && WbsMath.chance(MAX_FOOD_LOSS_CHANCE / level)) {
                        int foodLevel = player.getFoodLevel();
                        player.setFoodLevel(Math.max(foodLevel - 1, 0));
                    }

                    alreadyBroken.getWorld().spawnParticle(Particle.BLOCK,
                            alreadyBroken.getLocation(),
                            15,
                            0.5,
                            0.5,
                            0.5,
                            alreadyBroken.getBlockData());
                })
                .run(player, toBreak -> {
                    boolean result;
                    ItemStack held = player.getInventory().getItemInMainHand();

                    // Don't use durability on leaves
                    if (Tag.LEAVES.isTagged(toBreak.getType()) && held instanceof Damageable damageable && damageable.hasDamageValue()) {
                        int damage = damageable.getDamage();
                        result = player.breakBlock(toBreak);
                        damageable.setDamage(damage);
                        held.setItemMeta(damageable);
                    } else {
                        result = player.breakBlock(toBreak);
                    }
                    return result;
                });
    }
}
