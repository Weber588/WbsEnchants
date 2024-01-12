package wbs.enchants;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import me.sciguymjm.uberenchant.api.utils.UberConfiguration;
import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.util.UberRegistrable;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;

import java.util.*;

public abstract class WbsEnchantment extends UberEnchantment implements UberRegistrable {
    private static final Random RANDOM = new Random();

    private final String stringKey;

    protected double cost = EnchantsSettings.DEFAULT_COST;
    protected double costMultiplier = EnchantsSettings.DEFAULT_COST_MODIFIER;
    @NotNull
    protected Map<Integer, Double> costForLevel = new HashMap<>();
    protected double removalCost = EnchantsSettings.DEFAULT_REMOVAL_COST;
    protected double extractionCost = EnchantsSettings.DEFAULT_EXTRACT_COST;
    protected boolean canUseOnAnything = EnchantsSettings.DEFAULT_USABLE_ANYWHERE;
    @NotNull
    protected List<String> aliases = new LinkedList<>();

    public WbsEnchantment(String key) {
        super(new NamespacedKey(WbsEnchants.getInstance(), key));
        stringKey = key;
        EnchantsSettings.register(this);
    }

    @NotNull
    public abstract String getDescription();
    @NotNull
    public String getTargetDescription() {
        return switch (getItemTarget()) {
            case ARMOR_HEAD -> "Helmet";
            case ARMOR_TORSO -> "Chestplate";
            case ARMOR_LEGS -> "Leggings";
            case ARMOR_FEET -> "Boots";
            default -> WbsEnums.toPrettyString(getItemTarget());
        };
    }

    @Override
    public String getPermission() {
        return "wbsenchants.enchantment." + getName();
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @NotNull
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return getItemTarget().includes(itemStack);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return new NamespacedKey(WbsEnchants.getInstance(), stringKey);
    }

    @NotNull
    protected Map<NamespacedKey, Double> getLootKeyChances() {
        return new HashMap<>();
    }

    @Nullable
    public Double getAddToChance(LootTable table) {
        return getLootKeyChances().get(table.getKey());
    }

    public void onLootGenerate(LootGenerateEvent event) {
        Double chance = getAddToChance(event.getLootTable());

        if (chance == null) {
            return;
        }

        if (!WbsMath.chance(chance)) {
            return;
        }

        for (ItemStack stack : event.getLoot()) {
            int level;
            int maxLevel = getMaxLevel();
            if (maxLevel < 1) {
                level = 0;
            } else {
                level = RANDOM.nextInt(maxLevel) + 1;
            }
            if (tryAdd(stack, level)) {
                return;
            }
        }
    }

    public boolean matches(String asString) {
        if (stringKey.equalsIgnoreCase(asString)) {
            return true;
        }

        return getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(asString));
    }

    public boolean looselyMatches(String asString) {
        asString = asString.toLowerCase();
        if (stringKey.startsWith(asString)) {
            return true;
        }

        String finalAsString = asString;
        return getAliases().stream().anyMatch(alias -> alias.startsWith(finalAsString));
    }

    protected boolean tryAdd(ItemStack stack, int level) {
        if (stack.getType() != Material.ENCHANTED_BOOK && !canEnchantItem(stack)) {
            return false;
        }

        Set<Enchantment> existing = new HashSet<>();
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            existing = meta.getStoredEnchants().keySet();
        } else {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                existing = meta.getEnchants().keySet();
            }
        }

        for (Enchantment other : existing) {
            if (conflictsWith(other)) {
                return false;
            }
        }

        if (stack.getType() == Material.ENCHANTED_BOOK) {
            UberUtils.addStoredEnchantment(this, stack, level);
        } else {
            UberUtils.addEnchantment(this, stack, level);
        }

        return true;
    }

    public double getCost() {
        return EnchantsSettings.DEFAULT_COST;
    }

    public double getCostMultiplier() {
        return EnchantsSettings.DEFAULT_COST_MODIFIER;
    }

    @NotNull
    public Map<Integer, Double> getCostForLevel() {
        return new HashMap<>();
    }

    public double getRemovalCost() {
        return EnchantsSettings.DEFAULT_REMOVAL_COST;
    }

    public double getExtractionCost() {
        return EnchantsSettings.DEFAULT_EXTRACT_COST;
    }

    public boolean canUseOnAnything() {
        return EnchantsSettings.DEFAULT_USABLE_ANYWHERE;
    }

    public void registerUberRecord() {
        UberConfiguration.registerUberRecord(
                this,
                this.getCost(),
                this.getCostMultiplier(),
                this.getRemovalCost(),
                this.getExtractionCost(),
                this.canUseOnAnything(),
                this.getAliases(),
                this.getCostForLevel()
        );
    }

    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection section = baseFile.createSection(getName());

        section.set("min_level", getStartLevel());
        section.set("max_level", getMaxLevel());
        section.set("cost", getCost());
        section.set("cost_multiplier", getCostMultiplier());
        section.createSection("cost_for_level", getCostForLevel());
        section.set("removal_cost", getRemovalCost());
        section.set("extraction_cost", getExtractionCost());
        section.set("use_on_anything", canUseOnAnything());
        section.set("aliases", getAliases() );

        return section;
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        cost = section.getDouble("cost", getCost());
        costMultiplier = section.getDouble("cost_multiplier", getCostMultiplier());
        removalCost = section.getDouble("removal_cost", getRemovalCost());
        extractionCost = section.getDouble("extraction_cost", getExtractionCost());
        canUseOnAnything = section.getBoolean("use_on_anything", canUseOnAnything());

        aliases = section.getStringList("aliases");

        ConfigurationSection costLevelSection = section.getConfigurationSection("cost_for_level");

        if (costLevelSection != null) {
            for (String key : costLevelSection.getKeys(false)) {
                if (!key.matches("[0-9]")) {
                    WbsEnchants.getInstance().settings.logError("Invalid level: \"" + key + "\"", directory + "/cost_for_level");
                    continue;
                }

                int level;
                try {
                    level = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    // Should be impossible unless they put a number larger than int limit
                    WbsEnchants.getInstance().settings.logError("An unknown error occurred while parsing level: \""
                            + key + "\". Error: " + e.getLocalizedMessage(), directory + "/cost_for_level");
                    continue;
                }

                if (costLevelSection.isDouble(key) || costLevelSection.isInt(key)) {
                    costForLevel.put(level, costLevelSection.getDouble(key));
                }
            }
        }
    }

    /**
     * @return Whether or not this enchantment is under development, and should override user configuration.
     */
    public boolean developerMode() {
        return WbsEnchants.getInstance().settings.isDeveloperMode();
    }
}
