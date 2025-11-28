package wbs.enchants.events.enchanting;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import wbs.enchants.util.EnchantingEventUtils;

import java.util.List;

public class EnchantingPreparationContext extends EnchantingContext {
    private final @Nullable
    @Range(from = 0, to = 2) Integer slot;

    public EnchantingPreparationContext(Block enchantingBlock, Player enchanter, ItemStack item, int seed, @Nullable @Range(from = 0, to = 2) Integer slot, List<Block> powerProviderBlocks) {
        super(enchantingBlock, enchanter, item, seed, powerProviderBlocks);
        this.slot = slot;
    }

    public EnchantingPreparationContext(@NotNull Block enchantBlock, @NotNull Player enchanter, @NotNull ItemStack item, int seed, @Nullable @Range(from = 0, to = 2) Integer slot) {
        this(enchantBlock, enchanter, item, seed, slot, EnchantingEventUtils.getPowerProviderBlocks(enchantBlock));
    }

    public @Nullable @Range(from = 0, to = 2) Integer slot() {
        return slot;
    }
}
