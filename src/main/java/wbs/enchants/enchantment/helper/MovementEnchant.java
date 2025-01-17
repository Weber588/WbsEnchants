package wbs.enchants.enchantment.helper;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface MovementEnchant {
    void onChangeBlock(Player player, Block oldBlock, Block newBlock);
}
