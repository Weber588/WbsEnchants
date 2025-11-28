package wbs.enchants.events.enchanting;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ChooseEnchantmentHintEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final EnchantingPreparationContext context;
    private Enchantment chosenEnchantment;
    private final Map<Enchantment, Integer> enchantments;

    public ChooseEnchantmentHintEvent(EnchantingPreparationContext context, Enchantment chosenEnchantment, Map<Enchantment, Integer> enchantments) {
        this.context = context;
        this.chosenEnchantment = chosenEnchantment;
        this.enchantments = enchantments;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public Enchantment getChosenEnchantment() {
        return chosenEnchantment;
    }

    public void setChosenEnchantment(Enchantment chosenEnchantment) {
        this.chosenEnchantment = chosenEnchantment;
    }

    public EnchantingPreparationContext getContext() {
        return context;
    }

    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
