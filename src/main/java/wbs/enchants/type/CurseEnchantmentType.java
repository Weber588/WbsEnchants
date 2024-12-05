package wbs.enchants.type;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CurseEnchantmentType extends EnchantmentType {
    public CurseEnchantmentType() {
        super("Curse");
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.text("A curse enchantment, like from vanilla Minecraft. Cannot be removed via Grindstone.");
    }

    @Override
    public @Nullable TagKey<Enchantment> getTagKey() {
        return EnchantmentTagKeys.CURSE;
    }

    @Override
    public TextColor getColour() {
        return NamedTextColor.RED;
    }
}
