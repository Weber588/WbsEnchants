package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.BlockQueryUtils;

import java.util.List;
import java.util.function.Predicate;

public class ExcavatorEnchant extends AbstractMultiBreakEnchant {
    public ExcavatorEnchant() {
        super("excavator", Tag.ITEMS_SHOVELS);
    }

    @EventHandler
    @Override
    protected void catchEvent(BlockBreakEvent event) {
        onBreakBlock(event);
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

        final List<Block> excavateBlocks = BlockQueryUtils.getSquareMatching(broken, level, hitFace, matching);

        int toBreakPerChunk = excavateBlocks.size() / 3;

        BlockChanger.prepare(excavateBlocks)
                .setDelayTicks(1)
                .setToUpdatePerChunk(toBreakPerChunk)
                .setMatching(matching)
                .breakBlocks(player);
    }

    @Override
    public String getDisplayName() {
        return "&7Excavator";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public @NotNull String getDescription() {
        int maxWidth = getMaxLevel() * 2 + 1;
        return "Mines a 3x3 square at level 1, increasing the width by 2 per level, up to " + maxWidth +  " at level "
                + getMaxLevel() + ".";
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Shovel";
    }
}
