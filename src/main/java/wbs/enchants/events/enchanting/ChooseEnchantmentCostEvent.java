package wbs.enchants.events.enchanting;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ChooseEnchantmentCostEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final EnchantingPreparationContext context;
    private final int seed;
    private final int slot;
    private final int power;
    private int cost;

    public ChooseEnchantmentCostEvent(EnchantingPreparationContext context, int seed, int slot, int power, int cost) {
        this.context = context;
        this.seed = seed;
        this.slot = slot;
        this.power = power;
        this.cost = cost;
    }

    public int getPower() {
        return power;
    }

    public int getSlot() {
        return slot;
    }

    public int getSeed() {
        return seed;
    }

    public EnchantingPreparationContext getContext() {
        return context;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
