package wbs.enchants.events.enchanting;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FinalizeItemEnchantmentsEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final EnchantingEnchantContext context;
    private final EnchantItemEvent wrappedEvent;
    private final Map<Enchantment, Integer> enchantments;

    public FinalizeItemEnchantmentsEvent(EnchantingEnchantContext context, EnchantItemEvent wrappedEvent, Map<Enchantment, Integer> enchantments) {
        this.context = context;
        this.wrappedEvent = wrappedEvent;
        this.enchantments = enchantments;
    }

    public EnchantItemEvent getWrappedEvent() {
        return wrappedEvent;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public EnchantingEnchantContext getContext() {
        return context;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

}
