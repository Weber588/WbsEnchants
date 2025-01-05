package wbs.enchants;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.generation.ContextManager;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.string.RomanNumerals;

import java.util.*;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class EnchantmentDefinition implements Keyed {
    private static NamespacedKey parseKey(ConfigurationSection section, String key, String directory) {
        String keyString = section.getString(key);
        if (keyString != null) {
            return parseKey(keyString, directory + '/' + key);
        }

        return null;
    }
    private static NamespacedKey parseKey(@NotNull String keyString, String directory) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(keyString);
        if (namespacedKey != null) {
            return namespacedKey;
        } else {
            throw new InvalidConfigurationException("Invalid namespaced key: " + keyString, directory);
        }
    }

    private static <T extends org.bukkit.Keyed> TagKey<T> tryGetTagName(RegistryKeySet<@NotNull T> keySet) {
        if (keySet == null) {
            return null;
        }
        String tagSplit = "tagKey=#";
        String asString = keySet.toString();
        System.out.println("parsing " + asString);

        if (asString.contains(tagSplit)) {
            String keyString = keySet.toString().split(tagSplit)[1].split(" ")[0];
            System.out.println("keyString: " + keyString);
            NamespacedKey namespacedKey = NamespacedKey.fromString(keyString);
            if (namespacedKey == null) {
                return null;
            }

            return TagKey.create(keySet.registryKey(), namespacedKey);
        }

        return null;
    }

    @NotNull
    private final Key key;
    private boolean isEnabled = true;
    @NotNull
    private Component displayName;
    private @Nullable String description;
    private @Nullable String targetDescription;
    private int maxLevel = 1;
    private int weight = 1;
    @NotNull
    private EnchantmentRegistryEntry.EnchantmentCost minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(5, 8);
    @NotNull
    private EnchantmentRegistryEntry.EnchantmentCost maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(25, 8);
    private TagKey<ItemType> primaryItems = WbsEnchantsBootstrap.ITEM_EMPTY;
    private TagKey<ItemType> supportedItems = WbsEnchantsBootstrap.ITEM_EMPTY;
    private TagKey<Enchantment> exclusiveWith = WbsEnchantsBootstrap.ENCHANTMENT_EMPTY;
    private int anvilCost = 1;
    private EquipmentSlotGroup activeSlots = EquipmentSlotGroup.ANY;
    private List<TagKey<Enchantment>> injectInto = new LinkedList<>();

    protected final List<@NotNull GenerationContext> generationContexts = new LinkedList<>();
    private EnchantmentType type = EnchantmentTypeManager.REGULAR;

    public EnchantmentDefinition(@NotNull Key key,
                                 @NotNull Component displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    @Nullable
    public final String description() {
        return description == null ? null : description
                .replaceAll("%max_level%", String.valueOf(maxLevel));
    }

    @NotNull
    public Key key() {
        return key;
    }


    public EnchantmentDefinition setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    public EnchantmentDefinition displayName(@NotNull Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public EnchantmentDefinition description(@Nullable String description) {
        this.description = description;
        return this;
    }

    public EnchantmentDefinition targetDescription(@Nullable String targetDescription) {
        this.targetDescription = targetDescription;
        return this;
    }

    public EnchantmentDefinition maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    public EnchantmentDefinition weight(int weight) {
        this.weight = weight;
        return this;
    }

    public EnchantmentDefinition minimumCost(@NotNull EnchantmentRegistryEntry.EnchantmentCost minimumCost) {
        this.minimumCost = minimumCost;
        return this;
    }

    public EnchantmentDefinition maximumCost(@NotNull EnchantmentRegistryEntry.EnchantmentCost maximumCost) {
        this.maximumCost = maximumCost;
        return this;
    }

    public EnchantmentDefinition primaryItems(TagKey<ItemType> primaryItems) {
        this.primaryItems = primaryItems;
        return this;
    }

    public EnchantmentDefinition supportedItems(TagKey<ItemType> supportedItems) {
        this.supportedItems = supportedItems;
        return this;
    }

    public EnchantmentDefinition exclusiveWith(TagKey<Enchantment> exclusiveWith) {
        this.exclusiveWith = exclusiveWith;
        return this;
    }
    public EnchantmentDefinition anvilCost(int anvilCost) {
        this.anvilCost = anvilCost;
        return this;
    }

    public EnchantmentDefinition activeSlots(EquipmentSlotGroup activeSlots) {
        this.activeSlots = activeSlots;
        return this;
    }

    public EnchantmentDefinition addInjectInto(List<TagKey<Enchantment>> injectInto) {
        this.injectInto.addAll(injectInto);
        return this;
    }

    @SafeVarargs
    public final EnchantmentDefinition addInjectInto(TagKey<Enchantment> ... injectInto) {
        Collections.addAll(this.injectInto, injectInto);
        return this;
    }

    public EnchantmentDefinition setInjectInto(List<TagKey<Enchantment>> injectInto) {
        this.injectInto = injectInto;
        return this;
    }


    public EnchantmentDefinition exclusiveInject(TagKey<Enchantment> exclusiveWithInjectInto) {
        return this.exclusiveWith(exclusiveWithInjectInto)
                .addInjectInto(exclusiveWithInjectInto);
    }

    public EnchantmentDefinition type(EnchantmentType type) {
        this.type = type;
        return this;
    }

    public void configureBoostrap(ConfigurationSection section, String directory) {
        configureBoostrap(section, null, directory);
    }
    public void configureBoostrap(ConfigurationSection section, @Nullable EnchantmentRegistryEntry.Builder fallback, String directory) {
        configureFallback(fallback);

        isEnabled = section.getBoolean("enabled", isEnabled);
        String displayNameString = section.getString("display_name", GsonComponentSerializer.gson().serialize(displayName));
        try {
            displayName = GsonComponentSerializer.gson().deserialize(displayNameString);
        } catch(Exception ex) {
            try {
                displayName = MiniMessage.miniMessage().deserialize(displayNameString);
            } catch(Exception ignored) {}
        }

        NamespacedKey namespacedKey = parseKey(section, "supported_items", directory);
        if (namespacedKey != null) {
            supportedItems = TagKey.create(RegistryKey.ITEM, namespacedKey);
        }

        namespacedKey = parseKey(section, "primary_items", directory);
        if (namespacedKey != null) {
            primaryItems = TagKey.create(RegistryKey.ITEM, namespacedKey);
        }

        int minBaseCost = section.getInt("minimum_cost.base_cost", minimumCost.baseCost());
        int minExtraCost = section.getInt("minimum_cost.additional_per_level_cost", minimumCost.additionalPerLevelCost());

        minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(minBaseCost, minExtraCost);

        int maxBaseCost = section.getInt("maximum_cost.base_cost", maximumCost.baseCost());
        int maxExtraCost = section.getInt("maximum_cost.additional_per_level_cost", maximumCost.additionalPerLevelCost());

        maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(maxBaseCost, maxExtraCost);

        namespacedKey = parseKey(section, "exclusive_with", directory);
        if (namespacedKey != null) {
            exclusiveWith = TagKey.create(RegistryKey.ENCHANTMENT, namespacedKey);
        }

        anvilCost = section.getInt("anvil_cost", anvilCost);
        // TODO: Add hard coded "safe max level" field on implementors to let enchants limit to safe maximums?
        maxLevel = section.getInt("max_level", maxLevel);

        weight = section.getInt("weight", weight);

        List<String> injectIntoStrings = section.getStringList("inject_into");
        for (String injectIntoString : injectIntoStrings) {
            NamespacedKey injectionKey = parseKey(injectIntoString, directory + "/inject_into");
            injectInto.add(EnchantmentTagKeys.create(injectionKey));
        }

        NamespacedKey typeKey = parseKey(section, "type", directory);
        type = EnchantmentTypeManager.getType(typeKey);
    }

    private void configureFallback(EnchantmentRegistryEntry.@Nullable Builder fallback) {
        if (fallback != null) {
            // Load defaults
            displayName = fallback.description();

            TagKey<ItemType> checkItem = tryGetTagName(fallback.primaryItems());
            if (checkItem != null) {
                primaryItems = checkItem;
            }
            checkItem = tryGetTagName(fallback.supportedItems());
            if (checkItem != null) {
                supportedItems = checkItem;
            }

            TagKey<Enchantment> checkEnch = tryGetTagName(fallback.exclusiveWith());
            if (checkEnch != null) {
                exclusiveWith = checkEnch;
            }

            minimumCost = fallback.minimumCost();
            maximumCost = fallback.maximumCost();
            anvilCost = fallback.anvilCost();
            maxLevel = fallback.maxLevel();
            weight = fallback.weight();
        }
    }

    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        return buildConfigurationSection(baseFile, null);
    }
    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile, @Nullable EnchantmentRegistryEntry.Builder fallback) {
        ConfigurationSection section = baseFile.createSection(key().asString());
        configureFallback(fallback);

        section.set("enabled", isEnabled);
        if (displayName.hasStyling()) {
            section.set("display_name", GsonComponentSerializer.gson().serialize(displayName));
        } else {
            section.set("display_name", MiniMessage.miniMessage().serialize(displayName));
        }

        section.set("supported_items", supportedItems.key().toString());
        section.set("primary_items", primaryItems.key().toString());

        section.set("exclusive_with", exclusiveWith.key().toString());

        section.set("minimum_cost.base_cost", minimumCost.baseCost());
        section.set("minimum_cost.additional_per_level_cost", minimumCost.additionalPerLevelCost());
        section.set("maximum_cost.base_cost", maximumCost.baseCost());
        section.set("maximum_cost.additional_per_level_cost", maximumCost.additionalPerLevelCost());

        section.set("anvil_cost", anvilCost);
        section.set("max_level", maxLevel);
        section.set("weight", weight);

        section.set("inject_into", injectInto.stream().map(tagKey -> tagKey.key().toString()).toList());

        return section;
    }

    public void configure(ConfigurationSection section, String directory) {
        configure(section, null, directory);
    }

    public void configure(ConfigurationSection section, @Nullable EnchantmentRegistryEntry.Builder fallback, String directory) {
        configureBoostrap(section, fallback, directory);

        ConfigurationSection generationSection = section.getConfigurationSection("generation");
        if (generationSection != null) {
            generationContexts.clear();
            for (String key : generationSection.getKeys(false)) {
                ConfigurationSection contextSection = generationSection.getConfigurationSection(key);

                String contextDir = directory + "/generation/" + key;
                if (contextSection != null) {
                    try {
                        GenerationContext context = ContextManager.getContext(key,
                                this,
                                contextSection,
                                contextDir);

                        generationContexts.add(context);
                    } catch (InvalidConfigurationException ex) {
                        String errorDir = ex.getDirectory();
                        if (errorDir == null) {
                            errorDir = contextDir;
                        }

                        throw new InvalidConfigurationException(ex.getMessage(), contextDir);
                    }
                } else {
                    throw new InvalidConfigurationException("Generation type must be a section: " + key, contextDir);
                }
            }
        }
    }

    @NotNull
    public Enchantment getEnchantment() {
        Enchantment enchantment = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(this.key());

        if (enchantment == null) {
            throw new IllegalStateException("Server enchantment not found for enchantment \"" + this.key() + "\".");
        }

        return enchantment;
    }

    public boolean isEnchantmentOn(@NotNull ItemStack item) {
        return item.containsEnchantment(getEnchantment());
    }

    public boolean tryAdd(ItemStack stack, int level) {
        Enchantment enchantment = getEnchantment();
        if (stack.getType() != Material.ENCHANTED_BOOK && !enchantment.canEnchantItem(stack)) {
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
            if (enchantment.conflictsWith(other)) {
                return false;
            }
        }

        EnchantUtils.addEnchantment(this, stack, level);

        return true;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public List<GenerationContext> getGenerationContexts() {
        return new LinkedList<>(generationContexts);
    }

    /**
     * Provides a list of enchantment tags to add this to during registration. <br/>
     * Common (minecraft:) tags are:
     * <li>curse</li>
     * <li>in_enchanting_table</li>
     * <li>tradeable</li>
     * <li>on_random_loot</li>
     * <li>on_mob_spawn_equipment</li>
     * <li>double_trade_price</li>
     * <li>on_traded_equipment</li>
     * <li>treasure</li>
     * <li>non_treasure</li>
     * @return The enchantment tags this enchantment should be a part of
     */
    @NotNull
    public List<TagKey<Enchantment>> injectInto() {
        return injectInto;
    }

    public void buildTo(TagProducer tagProducer,
                        EnchantmentRegistryEntry.Builder builder) {
        builder.description(displayName)
                .supportedItems(tagProducer.getOrCreateTag(supportedItems))
                .primaryItems(tagProducer.getOrCreateTag(primaryItems))
                .minimumCost(minimumCost)
                .maximumCost(maximumCost)
                .activeSlots(activeSlots)
                .exclusiveWith(tagProducer.getOrCreateTag(exclusiveWith))
                .anvilCost(anvilCost)
                .maxLevel(maxLevel())
                .weight(weight);
    }

    public void registerGenerationContexts() {
        PluginManager manager = Bukkit.getPluginManager();
        WbsEnchants plugin = WbsEnchants.getInstance();
        for (GenerationContext context : generationContexts) {
            HandlerList.unregisterAll(context);
            manager.registerEvents(context, plugin);
        }
    }

    public Component getHoverText() {
        return getHoverText(null);
    }
    public Component getHoverText(@Nullable EnumSet<HoverOptions> options) {
        if (options == null) {
            options = EnumSet.allOf(HoverOptions.class);
        }

        WbsMessageBuilder builder = WbsEnchants.getInstance().buildMessage("&h&m        &h ")
                .append(displayName)
                .append(" &h&m        &h");

        if (options.contains(HoverOptions.MAX_LEVEL)) {
            builder.append("\n&rMax level: &h" + RomanNumerals.toRoman(maxLevel()) + " (" + maxLevel() + ")");
        }
        if (options.contains(HoverOptions.TARGET)) {
            builder.append("\n&rTarget: &h" + targetDescription);
        }
        if (options.contains(HoverOptions.DESCRIPTION)) {
            builder.append("\n&rDescription: &h" + description());
        }

        return builder.toComponent();
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public TagKey<ItemType> primaryItems() {
        return primaryItems;
    }

    public Component displayName() {
        return displayName;
    }

    public TypedKey<Enchantment> getTypedKey() {
        return TypedKey.create(RegistryKey.ENCHANTMENT, key());
    }

    public EnchantmentType type() {
        return type;
    }

    @NotNull
    public String targetDescription() {
        if (targetDescription == null) {
            if (supportedItems != null) {
                return "#" + supportedItems.key().asString();
            } else {
                return "Unknown";
            }
        }
        return targetDescription;
    }

    public interface TagProducer {
        <V extends org.bukkit.Keyed> Tag<@NotNull V> getOrCreateTag(TagKey<V> var1);
    }

    public enum HoverOptions {
        MAX_LEVEL,
        TARGET,
        DESCRIPTION
    }
}
