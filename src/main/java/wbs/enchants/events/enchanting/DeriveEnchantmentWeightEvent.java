package wbs.enchants.events.enchanting;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DeriveEnchantmentWeightEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final EnchantingContext context;
    private final Enchantment enchantment;
    private int weight;

    public DeriveEnchantmentWeightEvent(EnchantingContext context, Enchantment enchantment, int weight) {
        this.context = context;
        this.enchantment = enchantment;
        this.weight = weight;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getWeight() {
        return weight;
    }

    public DeriveEnchantmentWeightEvent setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public EnchantingContext getContext() {
        return context;
    }
}
