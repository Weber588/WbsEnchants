package wbs.enchants.enchantment.helper;

import org.bukkit.block.Block;

public interface TickableBlockEnchant extends TickableEnchant, BlockEnchant {
    void onTick(Block block);
}
