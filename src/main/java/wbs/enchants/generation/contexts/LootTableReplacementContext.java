package wbs.enchants.generation.contexts;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.LootGenerateEvent;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchants;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LootTableReplacementContext extends GenerationContext {
    public final List<Key> toReplace = new LinkedList<>();

    public LootTableReplacementContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);

        List<String> replaceStrings = section.getStringList("to-replace");
        replaceStrings.forEach(string -> {
            Key check = NamespacedKey.fromString(string);
            if (check == null) {
                WbsEnchants.getInstance().settings.logError("Invalid key: " + string, directory + "/to-replace");
                return;
            }

            toReplace.add(check);
        });

        if (toReplace.isEmpty()) {
            throw new InvalidConfigurationException("No valid replacement enchantments.", directory);
        }
    }

    @Override
    protected Component describeContext(TextComponent listBreak) {
        Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        List<Component> toReplace = this.toReplace.stream()
                .map(registry::get)
                .filter(Objects::nonNull)
                .map(EnchantUtils::getDisplayName)
                .toList();

        return Component.text("Replacing a below enchantment in a loot table: " + chanceToRun() + "%")
                .append(listBreak)
                .append(Component.join(JoinConfiguration.separator(listBreak), toReplace));
    }

    @Override
    public void writeToSection(ConfigurationSection section) {
        Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        section.set("to-replace", toReplace.stream().map(registry::get)
                .filter(Objects::nonNull)
                .map(Enchantment::key)
                .map(Key::asString)
                .toList());
    }

    @Override
    protected int getDefaultChance() {
        return 20;
    }

    @EventHandler
    public void onLootTableGenerate(LootGenerateEvent event) {
        if (shouldRun()) {
            Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
            event.getLoot().forEach(item -> {
                Map<Enchantment, Integer> enchantments = item.getEnchantments();

                boolean replacedAny = false;
                for (Enchantment ench : toReplace.stream().map(registry::get).toList()) {
                    if (enchantments.containsKey(ench)) {
                        item.removeEnchantment(ench);
                        replacedAny = true;
                        break;
                    }
                }

                if (replacedAny) {
                    definition.tryAdd(item, generateLevel());
                }
            });
        }
    }
}
