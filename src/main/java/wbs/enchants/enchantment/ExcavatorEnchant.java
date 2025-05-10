package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.AbstractMultiBreakEnchant;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.BlockQuery;

import java.util.List;
import java.util.function.Predicate;

public class ExcavatorEnchant extends AbstractMultiBreakEnchant {
    private static final String DEFAULT_DESCRIPTION = "Mines a 3x3 square at level 1, increasing the width by 2 per level.";

    public ExcavatorEnchant() {
        super("excavator", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
                .supportedItems(ItemTypeTagKeys.SHOVELS)
                .weight(10)
                .targetDescription("Shovel");
    }

    @Override
    protected boolean canBreak(Block block) {
        return Tag.MINEABLE_SHOVEL.isTagged(block.getType());
    }

    @Override
    protected void handleBreak(@NotNull BlockBreakEvent event, @NotNull Block broken, @NotNull Player player, @NotNull ItemStack item, int level) {
        Material type = broken.getType();
        Predicate<Block> matching = check -> check.getType() == type;

        BlockFace hitFace = getTargetBlockFace(player);

        final List<Block> excavateBlocks = new BlockQuery()
                .setPredicate(matching)
                .setMaxDistance(level)
                .getSquare(broken, hitFace);

        int toBreakPerChunk = excavateBlocks.size() / 3;

        BlockChanger.prepare(excavateBlocks)
                .setDelayTicks(1)
                .setToUpdatePerChunk(toBreakPerChunk)
                .setMatching(matching)
                .breakBlocks(player);
    }
}
