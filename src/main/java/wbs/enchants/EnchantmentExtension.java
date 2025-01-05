package wbs.enchants;

import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.type.EnchantmentType;

import java.util.EnumSet;
import java.util.List;

public interface EnchantmentExtension extends Keyed {
    EnchantmentDefinition getDefinition();

    default boolean isEnabled() {
        return getDefinition().isEnabled();
    }

    @Nullable
    default String description() {
        return getDefinition().description();
    }

    default Component getHoverText() {
        return getDefinition().getHoverText();
    }

    default @NotNull Key key() {
        return getDefinition().key();
    }

    default Component displayName() {
        return getDefinition().displayName();
    }

    default Component getHoverText(@Nullable EnumSet<EnchantmentDefinition.HoverOptions> options) {
        return getDefinition().getHoverText(options);
    }

    default List<GenerationContext> getGenerationContexts() {
        return getDefinition().getGenerationContexts();
    }

    default @NotNull Enchantment getEnchantment() {
        return getDefinition().getEnchantment();
    }

    default int maxLevel() {
        return getDefinition().maxLevel();
    }

    default TagKey<ItemType> primaryItems() {
        return getDefinition().primaryItems();
    }

    default boolean isEnchantmentOn(@NotNull ItemStack item) {
        return getDefinition().isEnchantmentOn(item);
    }

    default boolean tryAdd(ItemStack stack, int level) {
        return getDefinition().tryAdd(stack, level);
    }

    default void registerGenerationContexts() {
        getDefinition().registerGenerationContexts();
    }

    default TypedKey<Enchantment> getTypedKey() {
        return getDefinition().getTypedKey();
    }

    default EnchantmentType type() {
        return getDefinition().type();
    }

    default void configureBoostrap(ConfigurationSection section, String directory) {
        getDefinition().configureBoostrap(section, directory);
    }
    default void configure(ConfigurationSection section, String directory) {
        getDefinition().configure(section, directory);
    }
    default ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        return getDefinition().buildConfigurationSection(baseFile);
    }

    default NamespacedKey getKey() {
        return new NamespacedKey(key().namespace(), key().value());
    }

    default String targetDescription() {
        return getDefinition().targetDescription();
    }
}
