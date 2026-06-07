package wbs.enchants.events.enchanting;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class GenerateTableEnchantsEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Collection<@NotNull Enchantment> availableOnTable;
    private final EnchantingContext context;

    public GenerateTableEnchantsEvent(Collection<@NotNull Enchantment> availableOnTable, EnchantingContext context) {
        this.availableOnTable = availableOnTable;
        this.context = context;
    }

    public Collection<@NotNull Enchantment> getAvailableOnTable() {
        return availableOnTable;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public EnchantingContext getContext() {
        return context;
    }
}
