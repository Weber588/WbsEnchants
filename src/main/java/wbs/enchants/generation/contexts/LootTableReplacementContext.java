package wbs.enchants.generation.contexts;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.LootGenerateEvent;
import wbs.enchants.EnchantmentDefinition;
import wbs.enchants.WbsEnchants;
import wbs.enchants.generation.GenerationContext;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LootTableReplacementContext extends GenerationContext {
    public final List<Enchantment> toReplace = new LinkedList<>();

    public LootTableReplacementContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);

        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

        List<String> replaceStrings = section.getStringList("to-replace");
        replaceStrings.forEach(string -> {
            Key check = NamespacedKey.fromString(string);
            if (check == null) {
                WbsEnchants.getInstance().settings.logError("Invalid key: " + string, directory + "/to-replace");
                return;
            }

            Enchantment found = registry.get(check);
            if (found == null) {
                WbsEnchants.getInstance().settings.logError("Invalid enchantment key: " + key, directory + "/to-replace");
                return;
            }

            toReplace.add(found);
        });

        if (toReplace.isEmpty()) {
            throw new InvalidConfigurationException("No valid replacement enchantments.", directory);
        }
    }

    @Override
    public void writeToSection(ConfigurationSection section) {
        section.set("to-replace", toReplace.stream().map(Enchantment::key).map(Key::asString).toList());
    }

    @Override
    protected int getDefaultChance() {
        return 20;
    }

    @EventHandler
    public void onLootTableGenerate(LootGenerateEvent event) {
        if (shouldRun()) {
            event.getLoot().forEach(item -> {
                Map<Enchantment, Integer> enchantments = item.getEnchantments();

                boolean replacedAny = false;
                for (Enchantment ench : toReplace) {
                    if (enchantments.containsKey(ench)) {
                        item.removeEnchantment(ench);
                        replacedAny = true;
                    }
                }

                if (replacedAny) {
                    definition.tryAdd(item, generateLevel());
                }
            });
        }
    }
}
