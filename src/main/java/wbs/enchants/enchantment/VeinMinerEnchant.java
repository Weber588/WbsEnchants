package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.AbstractMultiBreakEnchant;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.BlockQueryUtils;
import wbs.enchants.util.MaterialUtils;

import java.util.List;
import java.util.function.Predicate;

public class VeinMinerEnchant extends AbstractMultiBreakEnchant {
    private static final int BLOCKS_PER_LEVEL = 6;
    private static final String DEFAULT_DESCRIPTION = "When mining an ore, if there are other ores of the same type " +
            "adjacent type, they'll be mined too (up to " + BLOCKS_PER_LEVEL + " blocks per level).";

    public VeinMinerEnchant() {
        super("vein_miner", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(4)
                .weight(5)
                .supportedItems(ItemTypeTagKeys.PICKAXES)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_MULTIMINER);
    }

    @Override
    protected boolean canBreak(Block block) {
        return MaterialUtils.isOre(block);
    }

    @Override
    public void handleBreak(@NotNull BlockBreakEvent event, @NotNull Block broken, @NotNull Player player, @NotNull ItemStack item, int level) {
        int blocksToBreak = level * BLOCKS_PER_LEVEL;

        Material type = broken.getType();
        Predicate<Block> matching = check -> check.getType() == type;

        final List<Block> veinBlocks = BlockQueryUtils.getVeinMatching(broken, blocksToBreak, matching);

        int toBreakPerChunk = veinBlocks.size() / 3;

        BlockChanger.prepare(veinBlocks)
                .setDelayTicks(1)
                .setToUpdatePerChunk(toBreakPerChunk)
                .setMatching(matching)
                .breakBlocks(player);
    }
}
