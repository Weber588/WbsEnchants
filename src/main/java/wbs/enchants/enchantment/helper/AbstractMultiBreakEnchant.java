package wbs.enchants.enchantment.helper;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.EventUtils;

public abstract class AbstractMultiBreakEnchant extends WbsEnchantment {


    public AbstractMultiBreakEnchant(@NotNull EnchantmentDefinition definition) {
        super(definition);

        configureDefinition();
    }

    public AbstractMultiBreakEnchant(String key, String description) {
        super(key, description);

        configureDefinition();
    }

    public AbstractMultiBreakEnchant(String key, String displayName, String description) {
        super(key, displayName, description);

        configureDefinition();
    }

    public AbstractMultiBreakEnchant(String key, EnchantmentType type, String description) {
        super(key, type, description);

        configureDefinition();
    }

    public AbstractMultiBreakEnchant(String key, EnchantmentType type, String displayName, String description) {
        super(key, type, displayName, description);

        configureDefinition();
    }

    private void configureDefinition() {
        getDefinition()
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_MULTIMINER);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        EventUtils.register(BlockBreakEvent.class, this::onBreakBlock);
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
            // TODO: Make this configurable
            if (!player.isSneaking()) {
                handleBreak(event, broken, player, item, level);
            }
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
