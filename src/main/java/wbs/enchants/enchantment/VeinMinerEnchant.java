package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.BlockQueryUtils;

import java.util.List;
import java.util.function.Predicate;

public class VeinMinerEnchant extends AbstractMultiBreakEnchant {
    private static final int BLOCKS_PER_LEVEL = 6;

    public VeinMinerEnchant() {
        super("vein_miner", Tag.ITEMS_PICKAXES);
    }

    // Have to catch it in this class because EventHandler reflection is used on the object, not the parents :(
    @EventHandler
    @Override
    protected void catchEvent(BlockBreakEvent event) {
        onBreakBlock(event);
    }

    @Override
    protected boolean canBreak(Block block) {
        return Tag.MINEABLE_PICKAXE.isTagged(block.getType());
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

    @Override
    public String getDisplayName() {
        return "&7Vein Miner";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 4;
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
        return enchantment == EnchantsSettings.BLAST_MINER;
    }

    @Override
    public @NotNull String getDescription() {
        return "When mining an ore, if there are other ores of the same type adjacent type, they'll be mined too " +
                "(up to " + BLOCKS_PER_LEVEL + " blocks per level).";
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Pickaxe";
    }
}
