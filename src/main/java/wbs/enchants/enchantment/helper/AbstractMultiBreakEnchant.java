package wbs.enchants.enchantment.helper;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.BlockChanger;

public abstract class AbstractMultiBreakEnchant extends WbsEnchantment {
    @Nullable
    private final Tag<Material> toolTag;

    public AbstractMultiBreakEnchant(String key) {
        this(key, null);
    }

    public AbstractMultiBreakEnchant(String key, @Nullable Tag<Material> toolTag) {
        super(key);
        this.toolTag = toolTag;
    }

    public final void onBreakBlock(BlockBreakEvent event) {
        Block broken = event.getBlock();
        if (!canBreak(broken)) {
            return;
        }

        Player player = event.getPlayer();

        if (BlockChanger.isPlayerBreaking(player, broken)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (containsEnchantment(item)) {
            int level = getLevel(item);
            handleBreak(event, broken, player, item, level);
        }
    }

    @SuppressWarnings("unused")
    protected abstract void catchEvent(BlockBreakEvent event);

    protected abstract boolean canBreak(Block block);

    protected abstract void handleBreak(@NotNull BlockBreakEvent event,
                                        @NotNull Block broken,
                                        @NotNull Player player,
                                        @NotNull ItemStack item,
                                        int level);

    @Nullable
    protected BlockFace getTargetBlockFace(Player player) {
        RayTraceResult traceResult = player.rayTraceBlocks(100);

        if (traceResult == null) {
            return null;
        }

        return traceResult.getHitBlockFace();
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        if (toolTag != null) {
            return toolTag.isTagged(itemStack.getType());
        } else {
            return super.canEnchantItem(itemStack);
        }
    }
}
