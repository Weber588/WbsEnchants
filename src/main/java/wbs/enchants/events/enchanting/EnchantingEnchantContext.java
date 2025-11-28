package wbs.enchants.events.enchanting;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import wbs.enchants.util.EnchantingEventUtils;

import java.util.List;

public class EnchantingEnchantContext extends EnchantingContext {
    private final @Range(from = 0, to = 2) int slot;
    private final int cost;

    public EnchantingEnchantContext(Block enchantingBlock, Player enchanter, ItemStack item, int seed, @Range(from = 0, to = 2) int slot, int cost, List<Block> powerProviderBlocks) {
        super(enchantingBlock, enchanter, item, seed, powerProviderBlocks);
        this.slot = slot;
        this.cost = cost;
    }

    public EnchantingEnchantContext(@NotNull Block enchantBlock, @NotNull Player enchanter, @NotNull ItemStack item, int seed, @Range(from = 0, to = 2) int slot, int cost) {
        this(enchantBlock, enchanter, item, seed, slot, cost, EnchantingEventUtils.getPowerProviderBlocks(enchantBlock));
    }

    public @Range(from = 0, to = 2) int slot() {
        return slot;
    }
    public int cost() {
        return cost;
    }
}
