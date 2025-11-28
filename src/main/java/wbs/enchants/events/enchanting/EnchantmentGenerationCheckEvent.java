package wbs.enchants.events.enchanting;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EnchantmentGenerationCheckEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final EnchantingContext context;
    private final int modifiedEnchantingLevel;
    private final Enchantment enchantment;
    private final int enchantmentLevel;
    private boolean isAllowed;

    public EnchantmentGenerationCheckEvent(EnchantingContext context, int modifiedEnchantingLevel, Enchantment enchantment, int enchantmentLevel, boolean isAllowed) {
        this.context = context;
        this.modifiedEnchantingLevel = modifiedEnchantingLevel;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
        this.isAllowed = isAllowed;
    }

    public boolean isAllowed() {
        return isAllowed;
    }

    public EnchantmentGenerationCheckEvent setAllowed(boolean allowed) {
        isAllowed = allowed;
        return this;
    }

    public int getModifiedEnchantingLevel() {
        return modifiedEnchantingLevel;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public int getEnchantmentLevel() {
        return enchantmentLevel;
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
