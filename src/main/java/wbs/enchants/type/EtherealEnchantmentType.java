package wbs.enchants.type;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public class EtherealEnchantmentType extends EnchantmentType {
    EtherealEnchantmentType() {
        super("Ethereal");
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.text("A powerful enchantment that is removed from your item on death.");
    }

    @Override
    public TextColor getColour() {
        return NamedTextColor.GOLD;
    }
}
