package wbs.enchants;

import me.sciguymjm.uberenchant.api.utils.UberConfiguration;
import wbs.enchants.enchantment.*;
import wbs.utils.util.plugin.WbsSettings;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class EnchantsSettings extends WbsSettings {

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

    public static void register(WbsEnchantment enchantment) {
        REGISTERED_ENCHANTMENTS.add(enchantment);
    }

    private final WbsEnchants plugin;
    protected EnchantsSettings(WbsEnchants plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public static List<WbsEnchantment> getRegistered() {
        return Collections.unmodifiableList(REGISTERED_ENCHANTMENTS);
    }

    @Override
    public void reload() {
        loadDefaultConfig("config.yml");
        loadEnchants();
    }

    private void loadEnchants() {
        // Create new file instance of our custom config
        File file = new File(plugin.getDataFolder(), "enchantments.yml");

        REGISTERED_ENCHANTMENTS.forEach(enchant -> {
            // Register Lightning Strike with UberEnchant and add to UberRecords
            UberConfiguration.registerUberRecord(
                    enchant,			// Create new instance of Lightning Strike
                    1000.0,				// Set cost to use via UberEnchant
                    0.4,				// Set cost multiplier
                    100.0,				// Set removal cost
                    2000.0,				// Set extraction cost
                    false,				// Set if can use on anything
                    enchant.getAliases(),		// Set alaises (Can be empty)
                    new HashMap<>());		// Set Cost at level (Can be empty)
        });

        // Save to our config
        UberConfiguration.saveToFile(plugin, file);

        // Check if our file exists
        if (file.exists()) {
            // Load our config
            UberConfiguration.loadFromFile(file);
        }
    }
}
