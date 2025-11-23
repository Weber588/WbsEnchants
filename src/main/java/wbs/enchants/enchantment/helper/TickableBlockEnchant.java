package wbs.enchants.enchantment.helper;

import org.bukkit.block.Block;

import java.util.Map;

public interface TickableBlockEnchant extends TickableEnchant, BlockEnchant {
    void onTick(Block block, int level);

    default void tickAll(Map<Block, Integer> tickingEnchantedBlocks) {}
}
