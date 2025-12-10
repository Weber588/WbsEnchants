package wbs.enchants.definition;

import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.enchantment.helper.ItemModificationEnchant;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.type.EnchantmentType;

import java.util.List;

@SuppressWarnings("unused")
public interface EnchantmentExtension extends Keyed {
    EnchantmentDefinition getDefinition();

    default boolean isEnabled() {
        return getDefinition().isEnabled();
    }

    @Nullable
    default Component description() {
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

    default Component getHoverText(@Nullable List<DescribeOption> options) {
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

    default boolean isEnchantmentOn(@NotNull ItemStack item) {
        return getDefinition().isEnchantmentOn(item);
    }

    default boolean tryAdd(ItemStack stack, int level) {
        boolean added = getDefinition().tryAdd(stack, level);

        if (added) {
            if (this instanceof ItemModificationEnchant itemModEnchant) {
                itemModEnchant.validateUpdateItem(stack);
            }
        }

        return added;
    }

    default void registerGenerationContexts() {
        getDefinition().registerGenerationContexts();
    }

    default TypedKey<Enchantment> getTypedKey() {
        return getDefinition().getTypedKey();
    }

    default EnchantmentType type() {
        return getDefinition().getType();
    }

    default void configureBoostrap(ConfigurationSection section, String directory) {
        getDefinition().configureBoostrap(section, directory);
    }
    default void configure(@NotNull ConfigurationSection section, String directory) {
        getDefinition().configure(section, directory);
    }
    @NotNull
    default ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        return getDefinition().buildConfigurationSection(baseFile);
    }

    default NamespacedKey getKey() {
        return new NamespacedKey(key().namespace(), key().value());
    }
}
