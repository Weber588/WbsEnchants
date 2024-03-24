package wbs.enchants;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.enchants.util.EnchantmentManager;
import wbs.utils.util.plugin.WbsSettings;

public class EnchantsSettings extends WbsSettings {

    public static final double DEFAULT_COST = 1000;
    public static final double DEFAULT_COST_MODIFIER = 1000;
    public static final double DEFAULT_REMOVAL_COST = 100;
    public static final double DEFAULT_EXTRACT_COST = 2000;
    public static final boolean DEFAULT_USABLE_ANYWHERE = false;

    public static final String ENCHANT_FILE_NAME = "enchantments.yml";

    private YamlConfiguration enchantsFile;

    private boolean developerMode = false;

    protected EnchantsSettings(WbsEnchants plugin) {
        super(plugin);
    }

    @Override
    public void reload() {
        loadConfig();
        loadEnchants();
    }

    private void loadConfig() {
        YamlConfiguration config = loadDefaultConfig("config.yml");

        developerMode = config.getBoolean("developer-mode", developerMode);
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    private void loadEnchants() {
        enchantsFile = loadConfigSafely(genConfig(ENCHANT_FILE_NAME));

        boolean newEnchantAdded = false;
        for (WbsEnchantment enchant : EnchantmentManager.getRegistered()) {
            ConfigurationSection enchantSection = enchantsFile.getConfigurationSection(enchant.getName());

            if (enchantSection == null) {
                newEnchantAdded = true;
                enchant.buildConfigurationSection(enchantsFile);
            } else {
                enchant.configure(enchantSection, enchantsFile.getName() + "/" + enchant.getName());
            }

            if (!enchant.isEnabled()) {
                continue;
            }
            
            if (enchant.register()) {
                enchant.registerGenerationContexts();
            } else {
                logError("Enchantment " + enchant.getName() + " failed to register. See logs.", "Internal");
            }
        }

        if (newEnchantAdded) {
            saveEnchants();
        }
    }

    private void saveEnchants() {
        enchantsFile = saveYamlData(enchantsFile, "enchantments.yml", "enchantment", safeYaml ->
                EnchantmentManager.getRegistered().forEach(enchant -> {
                    ConfigurationSection enchantSection = safeYaml.getConfigurationSection(enchant.getName());

                    // Only write to config if the file doesn't already contain a definition for enchants -- don't want
                    // to override user configuration. (Or write if the enchant is marked as "in development" and should
                    // override every time.
                    if (enchant.developerMode() || enchantSection == null) {
                        enchant.buildConfigurationSection(safeYaml);
                    }
                })
            );
    }
}
