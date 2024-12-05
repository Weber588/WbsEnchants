package wbs.enchants.type;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public class ParadoxicalEnchantmentType extends EnchantmentType {
    ParadoxicalEnchantmentType() {
        super("Paradoxical");
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.text("An enchantment with both positives and negatives, usually quite powerful " +
                "with a drawback, or situationally/dubiously useful.");
    }

    @Override
    public TextColor getColour() {
        return NamedTextColor.DARK_PURPLE;
    }
}
