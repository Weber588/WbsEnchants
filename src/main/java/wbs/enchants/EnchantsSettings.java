package wbs.enchants;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.plugin.WbsSettings;

@SuppressWarnings("unused")
public class EnchantsSettings extends WbsSettings {

    private YamlConfiguration enchantsFile;

    private boolean developerMode = false;

    protected EnchantsSettings(WbsEnchants plugin) {
        super(plugin);
    }

    @Override
    public void reload() {
        loadConfig();
    //    EnchantManager.buildDatapack();
        loadEnchants();
        EnchantmentTypeManager.getRegistered().forEach(EnchantmentType::registerListeners);
    }

    private void loadConfig() {
        YamlConfiguration config = loadDefaultConfig("config.yml");

        developerMode = config.getBoolean("developer-mode", developerMode);
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    // TODO: Move this into the bootstrap phase
    private void loadEnchants() {
        enchantsFile = loadConfigSafely(genConfig("enchantments.yml"));

        boolean newEnchantAdded = false;
        for (WbsEnchantment enchant : EnchantManager.getCustomRegistered()) {
            ConfigurationSection enchantSection = enchantsFile.getConfigurationSection(enchant.key().value());

            if (enchantSection == null) {
                newEnchantAdded = true;
                enchant.buildConfigurationSection(enchantsFile);
            } else {
                enchant.configure(enchantSection, enchantsFile.getName() + "/" + enchant.key().value());
            }

            if (!enchant.isEnabled()) {
                continue;
            }

            enchant.registerGenerationContexts();
            enchant.registerEvents();
        }

        if (newEnchantAdded) {
            saveEnchants();
        }
    }

    private void saveEnchants() {
        enchantsFile = saveYamlData(enchantsFile, "enchantments.yml", "enchantment", safeYaml ->
                EnchantManager.getCustomRegistered().forEach(enchant -> {
                    ConfigurationSection enchantSection = safeYaml.getConfigurationSection(enchant.key().value());

                    // Only write to config if the file doesn't already contain a definition for enchants -- don't want
                    // to override user configuration. (Or write if the enchantment is marked as "in development" and should
                    // override every time.
                    if (enchant.developerMode() || enchantSection == null) {
                        enchant.buildConfigurationSection(safeYaml);
                    }
                })
            );
    }
}
