package wbs.enchants.events.enchanting;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GenerateTableEnchantsEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final long seed;
    private final ItemStack stack;
    private final int salt;
    private final int cost;

    public GenerateTableEnchantsEvent(long seed, ItemStack stack, int salt, int cost) {
        this.seed = seed;
        this.stack = stack;
        this.salt = salt;
        this.cost = cost;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
