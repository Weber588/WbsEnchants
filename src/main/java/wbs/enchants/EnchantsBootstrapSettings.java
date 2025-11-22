package wbs.enchants;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.jspecify.annotations.NullMarked;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.utils.util.WbsFileUtil;
import wbs.utils.util.plugin.bootstrap.WbsBootstrapSettings;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
public class EnchantsBootstrapSettings extends WbsBootstrapSettings<WbsEnchants> {
    @SuppressWarnings("NotNullFieldNotInitialized")
    private static EnchantsBootstrapSettings INSTANCE;
    public static EnchantsBootstrapSettings getInstance() {
        return INSTANCE;
    }

    public EnchantsBootstrapSettings(BootstrapContext context) {
        super(context, WbsEnchants.class);

        INSTANCE = this;
    }

    @Override
    public void reload() {
        registerEnchantmentEvents();
    }

    private void registerEnchantmentEvents() {
        File dataDirectory = context.getDataDirectory().toFile();
        if (!dataDirectory.exists()) {
            if (!dataDirectory.mkdir()) {
                context.getLogger().error("Failed to generate data directory!");
            }
        }

        loadEnchants();
        configureExternalEnchantments();
        registerCustomEnchantments();
    }

    private File enchantsFile;
    private YamlConfiguration enchantsConfig;

    private File externalEnchantsFile;
    private YamlConfiguration externalEnchantsConfig;
    public boolean newExternalEnchants = false;

    private void configureExternalEnchantments() {
        BootstrapContext context = getContext();

        String fileName = "external_enchantments.yml";

        externalEnchantsFile = new File(context.getDataDirectory().toFile(), fileName);
        if (!externalEnchantsFile.exists()) {
            WbsFileUtil.saveResource(context, WbsEnchants.class, fileName, false);
        }
        externalEnchantsConfig = loadConfigSafely(externalEnchantsFile);

        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
            EnchantmentRegistryEntry.Builder builder = event.builder();

            TypedKey<Enchantment> addedKey = event.key();
            WbsEnchantment customEnchantment = EnchantManager.getCustomFromKey(addedKey.key());
            if (customEnchantment == null) {
                // Not a custom enchantment -- check external file and add if not present.
                String stringKey = addedKey.key().asString();

                EnchantmentDefinition definition = new EnchantmentDefinition(addedKey.key(), event.builder().description());

                ConfigurationSection definitionSection = externalEnchantsConfig.getConfigurationSection(stringKey);
                try {
                    if (definitionSection == null) {
                        definition.buildConfigurationSection(externalEnchantsConfig, builder);
                        newExternalEnchants = true;
                    } else {
                        definition.configureBoostrap(definitionSection, builder, fileName + "/" + stringKey);

                        definition.buildTo(event::getOrCreateTag, builder);
                    }
                } catch (wbs.utils.exceptions.InvalidConfigurationException ex) {
                    context.getLogger().warn("Failed to parse external enchantment ({}):", stringKey);
                    context.getLogger().warn(ex.getMessage());
                    return;
                }

                EnchantManager.registerExternal(definition);
            }
        }));
    }

    private void loadEnchants() {
        String fileName = "enchantments.yml";

        enchantsFile = new File(context.getDataDirectory().toFile(), fileName);
        if (!this.enchantsFile.exists()) {
            WbsFileUtil.saveResource(context, WbsEnchants.class, fileName, false);
        }
        enchantsConfig = loadConfigSafely(enchantsFile);

        boolean newEnchantAdded = false;
        for (WbsEnchantment enchant : EnchantManager.getCustomRegistered()) {
            ConfigurationSection enchantSection = enchantsConfig.getConfigurationSection(enchant.key().value());

            if (enchantSection == null) {
                enchantSection = enchantsConfig.getConfigurationSection(enchant.key().asString());
            }
            if (enchantSection == null) {
                newEnchantAdded = true;
                enchant.buildConfigurationSection(enchantsConfig);
            } else {
                enchant.configure(enchantSection, enchantsConfig.getName() + "/" + enchant.key().value());
            }
        }

        if (newEnchantAdded) {
            saveEnchants();
        }
    }

    private void saveEnchants() {
        YamlConfiguration result = saveYamlData(enchantsConfig, "enchantments.yml", "enchantment", safeYaml ->
                EnchantManager.getCustomRegistered().forEach(enchant -> {
                    ConfigurationSection enchantSection = safeYaml.getConfigurationSection(enchant.key().value());

                    if (enchantSection == null) {
                        enchantSection = safeYaml.getConfigurationSection(enchant.key().asMinimalString());
                    }

                    // Only write to config if the file doesn't already contain a definition for enchants -- don't want
                    // to override user configuration. (Or write if the enchantment is marked as "in development" and should
                    // override every time.)
                    if (enchant.developerMode() || enchantSection == null) {
                        enchant.buildConfigurationSection(safeYaml);
                    }
                })
        );

        if (result != null) {
            enchantsConfig = result;
        }
    }
    private void registerCustomEnchantments() {
        LifecycleEventManager<BootstrapContext> manager = context.getLifecycleManager();
        File customEnchantsFile = new File(context.getDataDirectory().toFile(), "enchantments.yml");
        YamlConfiguration enchantsConfig;
        if (customEnchantsFile.exists()) {
            enchantsConfig = loadConfigSafely(customEnchantsFile);
        } else {
            enchantsConfig = null;
        }

        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
            List<WbsEnchantment> invalidEnchantments = new LinkedList<>();
            List<WbsEnchantment> disabledEnchants = new LinkedList<>();
            for (WbsEnchantment enchant : EnchantManager.getCustomRegistered()) {
                if (enchant.getDefinition().getSupportedItems() == null) {
                    context.getLogger().error("Failed to load custom enchantment {} -- supportedItems is required prior to registration.", enchant.key().asString());
                    invalidEnchantments.add(enchant);
                    continue;
                }

                if (enchantsConfig != null) {
                    ConfigurationSection enchantSection = enchantsConfig.getConfigurationSection(enchant.key().value());
                    if (enchantSection == null) {
                        enchantSection = enchantsConfig.getConfigurationSection(enchant.key().asString());
                    }
                    if (enchantSection == null) {
                        enchant.buildConfigurationSection(enchantsConfig);
                    } else {
                        try {
                            enchant.configureBoostrap(enchantSection, customEnchantsFile.getName() + "/" + enchant.key().value());
                        } catch (wbs.utils.exceptions.InvalidConfigurationException ex) {
                            context.getLogger().error(ex.getMessage());
                        }
                    }
                }

                if (enchant.isEnabled()) {
                    event.registry().register(
                            TypedKey.create(RegistryKey.ENCHANTMENT, enchant.key()),
                            builder -> enchant.getDefinition().buildTo(event::getOrCreateTag, builder)
                    );
                } else {
                    disabledEnchants.add(enchant);
                }
            }

            if (!invalidEnchantments.isEmpty()) {
                invalidEnchantments.forEach(EnchantManager::unregister);
                context.getLogger().error("Unregistered {} invalid enchantments.", invalidEnchantments.size());
            }

            if (!disabledEnchants.isEmpty()) {
                disabledEnchants.forEach(EnchantManager::unregister);
                context.getLogger().info("Unregistered {} disabled enchantments.", disabledEnchants.size());
            }
        }));
    }
}
