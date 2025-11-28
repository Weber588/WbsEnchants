package wbs.enchants.events.enchanting;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EnchantingDerivePowerEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final EnchantingPreparationContext context;
    private int power;
    private int maxPower;

    public EnchantingDerivePowerEvent(EnchantingPreparationContext context, int power, int maxPower) {
        this.context = context;
        this.power = power;
        this.maxPower = maxPower;
    }

    public EnchantingPreparationContext getContext() {
        return context;
    }

    public int getPower() {
        return power;
    }

    public EnchantingDerivePowerEvent setPower(int power) {
        this.power = power;
        return this;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public EnchantingDerivePowerEvent setMaxPower(int maxPower) {
        this.maxPower = maxPower;
        return this;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
