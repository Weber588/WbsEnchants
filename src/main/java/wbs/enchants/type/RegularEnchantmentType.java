package wbs.enchants.type;

import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegularEnchantmentType extends EnchantmentType {
    public RegularEnchantmentType() {
        super("Regular");
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.text("A regular enchantment, like most in vanilla minecraft.");
    }
    @Override
    public @Nullable TagKey<Enchantment> getTagKey() {
        // Don't create a Tag for this, since we can't add non-curse enchants from vanilla explicitly.
        // Instead, we'll determine if an enchant is "regular" by checking all other types first.
        return null;
    }

    @Override
    public TextColor getColour() {
        return NamedTextColor.GRAY;
    }
}
