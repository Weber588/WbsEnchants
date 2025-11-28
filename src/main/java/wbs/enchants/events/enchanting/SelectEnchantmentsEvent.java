package wbs.enchants.events.enchanting;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SelectEnchantmentsEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final EnchantingContext context;
    private final Map<Enchantment, Integer> enchantments;
    private final int cost;
    private final int slot;

    public SelectEnchantmentsEvent(EnchantingContext context, Map<Enchantment, Integer> enchantments, int cost, int slot) {
        this.context = context;
        this.enchantments = enchantments;
        this.cost = cost;
        this.slot = slot;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public EnchantingContext getContext() {
        return context;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public int getCost() {
        return cost;
    }

    public int getSlot() {
        return slot;
    }
}
