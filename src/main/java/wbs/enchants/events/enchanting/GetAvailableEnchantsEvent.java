package wbs.enchants.events.enchanting;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GetAvailableEnchantsEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final EnchantingContext context;
    private final Set<@NotNull Enchantment> availableOnTable;
    private final ItemStack stack;

    public GetAvailableEnchantsEvent(EnchantingContext context, Set<@NotNull Enchantment> availableOnTable, ItemStack stack) {
        this.context = context;
        this.availableOnTable = availableOnTable;
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    public EnchantingContext getContext() {
        return context;
    }

    public Set<Enchantment> getAvailableOnTable() {
        return availableOnTable;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
