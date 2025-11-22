package wbs.enchants;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.plugin.WbsSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"unused", "UnstableApiUsage"})
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

    public static boolean isPrimaryItem(ItemStack item) {
        ItemType itemType = item.getType().asItemType();
        return isPrimaryItem(itemType);
    }

    public static boolean isPrimaryItem(ItemType itemType) {
        TypedKey<ItemType> itemKey = TypedKey.create(RegistryKey.ITEM, Objects.requireNonNull(itemType).key());
        return isPrimaryItem(itemKey);
    }

    private static final Map<TypedKey<ItemType>, Boolean> PRIMARY_ITEMS = new HashMap<>();

    public static boolean isPrimaryItem(TypedKey<ItemType> itemKey) {
        Boolean isPrimaryItem = PRIMARY_ITEMS.get(itemKey);

        if (isPrimaryItem == null) {
            isPrimaryItem = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream()
                    .anyMatch(enchant -> {
                        RegistryKeySet<@NotNull ItemType> primaryItems = enchant.getPrimaryItems();
                        if (primaryItems != null) {
                            return primaryItems.contains(itemKey);
                        }
                        return false;
                    });

            PRIMARY_ITEMS.put(itemKey, isPrimaryItem);
        }

        return isPrimaryItem;
    }
}
