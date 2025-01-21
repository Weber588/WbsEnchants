package wbs.enchants.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.BlockEnchant;

public class EnchantedBlockBreakEvent extends BlockEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final BlockEnchant enchant;

    public EnchantedBlockBreakEvent(BlockEnchant enchant, @NotNull Block theBlock) {
        super(theBlock);
        this.enchant = enchant;
    }

    public BlockEnchant getEnchant() {
        return enchant;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
