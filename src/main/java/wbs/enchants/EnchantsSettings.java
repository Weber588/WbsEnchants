package wbs.enchants;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.enchants.enchantment.*;
import wbs.utils.util.plugin.WbsSettings;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class EnchantsSettings extends WbsSettings {

    public static final double DEFAULT_COST = 1000;
    public static final double DEFAULT_COST_MODIFIER = 1000;
    public static final double DEFAULT_REMOVAL_COST = 100;
    public static final double DEFAULT_EXTRACT_COST = 2000;
    public static final boolean DEFAULT_USABLE_ANYWHERE = false;

    private static final List<WbsEnchantment> REGISTERED_ENCHANTMENTS = new LinkedList<>();

    public static final EntangledEnchant ENTANGLED = new EntangledEnchant();
    public static final ExcavatorEnchant EXCAVATOR = new ExcavatorEnchant();
    public static final HarvesterEnchant HARVESTER = new HarvesterEnchant();
    public static final VampiricEnchant VAMPIRIC = new VampiricEnchant();
    public static final VeinMinerEnchant VEIN_MINER = new VeinMinerEnchant();
    public static final BlastMinerEnchant BLAST_MINER = new BlastMinerEnchant();
    public static final VoidWalkerEnchant VOID_WALKER = new VoidWalkerEnchant();
    public static final PlanarBindingEnchant PLANAR_BINDING = new PlanarBindingEnchant();
    public static final DisarmingEnchant DISARMING = new DisarmingEnchant();
    public static final FrenziedEnchant FRENZIED = new FrenziedEnchant();
    public static final LightweightEnchant LIGHTWEIGHT = new LightweightEnchant();
    public static final HellborneEnchant HELLBORNE = new HellborneEnchant();
    public static final DecayEnchant DECAY = new DecayEnchant();
    public static final ScorchingEnchant SCORCHING = new ScorchingEnchant();
    public static final FrostburnEnchant FROSTBURN = new FrostburnEnchant();
    public static final EnderShotEnchant ENDER_SHOT = new EnderShotEnchant();
    public static final ImmortalEnchant IMMORTAL = new ImmortalEnchant();

    public static void register(WbsEnchantment enchantment) {
        REGISTERED_ENCHANTMENTS.add(enchantment);
    }

    public static List<WbsEnchantment> getRegistered() {
        return Collections.unmodifiableList(REGISTERED_ENCHANTMENTS);
    }

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
        enchantsFile = loadConfigSafely(genConfig("enchantments.yml"));

        REGISTERED_ENCHANTMENTS.forEach(enchant -> {
            ConfigurationSection enchantSection = enchantsFile.getConfigurationSection(enchant.getName());

            if (enchantSection == null) {
                enchant.buildConfigurationSection(enchantsFile);
            } else {
                enchant.configure(enchantSection, enchantsFile.getName() + "/" + enchant.getName());
            }

            enchant.registerUberRecord();
        });
    }

    private void saveEnchants() {
        enchantsFile = saveYamlData(enchantsFile, "enchantments.yml", "enchantment", safeYaml ->
                REGISTERED_ENCHANTMENTS.forEach(enchant -> {
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
