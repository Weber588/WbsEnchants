package wbs.enchants;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.plugin.WbsSettings;

@SuppressWarnings("unused")
public class EnchantsSettings extends WbsSettings {

    protected EnchantsSettings(WbsEnchants plugin) {
        super(plugin);
    }

    @Override
    public void reload() {
        loadConfig();
    //    EnchantManager.buildDatapack();
        EnchantmentTypeManager.getRegistered().forEach(EnchantmentType::registerListeners);
    }

    private void loadConfig() {
        YamlConfiguration config = loadDefaultConfig("config.yml");

        developerMode = config.getBoolean("developer-mode", developerMode);
        forceOnlyLootEnchants = config.getBoolean("force-only-loot-enchants", forceOnlyLootEnchants);
        disableAnvilRepairPenalty = config.getBoolean("disable-anvil-repair-penalty", disableAnvilRepairPenalty);

        ConfigurationSection enchantabilitySection = config.getConfigurationSection("custom-enchantability");

        if (enchantabilitySection != null) {
            addEnchantability = enchantabilitySection.getBoolean("add-enchantable-for-primary-items", addEnchantability);
            defaultEnchantability = enchantabilitySection.getInt("default-enchantability", defaultEnchantability);
        }
    }

    private boolean developerMode = false;
    public boolean isDeveloperMode() {
        return developerMode;
    }

    private boolean disableAnvilRepairPenalty = false;
    public boolean disableAnvilRepairPenalty() {
        return disableAnvilRepairPenalty;
    }

    private boolean forceOnlyLootEnchants = false;
    public boolean forceOnlyLootEnchants() {
        return forceOnlyLootEnchants;
    }

    private boolean addEnchantability = true;
    public boolean addEnchantability() {
        return addEnchantability;
    }

    private int defaultEnchantability = 15;
    public int defaultEnchantability() {
        return defaultEnchantability;
    }
}
