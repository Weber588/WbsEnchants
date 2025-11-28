package wbs.enchants.events.enchanting;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.util.EnchantingEventUtils;

import java.util.List;

public class EnchantingContext {
    private final Block enchantingBlock;
    private final Player enchanter;
    private final ItemStack item;
    private final int seed;
    private final List<Block> powerProviderBlocks;

    public EnchantingContext(Block enchantingBlock, Player enchanter, ItemStack item, int seed, List<Block> powerProviderBlocks) {
        this.enchantingBlock = enchantingBlock;
        this.enchanter = enchanter;
        this.item = item;
        this.seed = seed;
        this.powerProviderBlocks = powerProviderBlocks;
    }

    public EnchantingContext(@NotNull Block enchantBlock, @NotNull Player enchanter, @NotNull ItemStack item, int seed) {
        this(enchantBlock, enchanter, item, seed, EnchantingEventUtils.getPowerProviderBlocks(enchantBlock));
    }

    public Block enchantingBlock() {
        return enchantingBlock;
    }

    public Player enchanter() {
        return enchanter;
    }

    public ItemStack item() {
        return item;
    }

    public int seed() {
        return seed;
    }

    public List<Block> powerProviderBlocks() {
        return powerProviderBlocks;
    }
}
