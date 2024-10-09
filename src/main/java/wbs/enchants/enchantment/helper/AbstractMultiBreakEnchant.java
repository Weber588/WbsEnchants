package wbs.enchants.enchantment.helper;

import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.EventUtils;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractMultiBreakEnchant extends WbsEnchantment {
    public AbstractMultiBreakEnchant(String key, @NotNull String description) {
        super(key, description);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        EventUtils.register(BlockBreakEvent.class, this::onBreakBlock);
    }

    @Override
    public @NotNull List<TagKey<Enchantment>> addToTags() {
        LinkedList<TagKey<Enchantment>> tags = new LinkedList<>(super.addToTags());

        tags.add(WbsEnchantsBootstrap.EXCLUSIVE_SET_MULTIMINER);

        return tags;
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

        if (isEnchantmentOn(item)) {
            int level = getLevel(item);
            handleBreak(event, broken, player, item, level);
        }
    }

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
}
