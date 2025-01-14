package wbs.enchants.definition;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchants;
import wbs.enchants.definition.TaggableRegistryKeySet.TaggableItemKeySet;
import wbs.enchants.enchantment.helper.ConflictEnchantment;
import wbs.enchants.generation.ContextManager;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.string.RomanNumerals;

import java.util.*;
import java.util.function.Consumer;

@SuppressWarnings({"UnstableApiUsage", "unused", "UnusedReturnValue"})
public class EnchantmentDefinition extends EnchantmentWrapper implements Comparable<EnchantmentDefinition> {
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

        if (asString.contains(tagSplit)) {
            String keyString = keySet.toString().split(tagSplit)[1].split(" ")[0];
            NamespacedKey namespacedKey = NamespacedKey.fromString(keyString);
            if (namespacedKey != null) {
                return TagKey.create(keySet.registryKey(), namespacedKey);
            }
        }

        return null;
    }

    private boolean isEnabled = true;
    @NotNull
    private Component displayName;
    private @Nullable Component description;
    private @Nullable Component targetDescription;

    @Nullable
    private TaggableItemKeySet primaryItems;
    @Nullable
    private TaggableItemKeySet supportedItems;
    @Nullable
    private TaggableRegistryKeySet<Enchantment> exclusiveWith;

    private int maxLevel = 1;
    private int weight = 1;
    @NotNull
    private EnchantmentRegistryEntry.EnchantmentCost minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(5, 8);
    @NotNull
    private EnchantmentRegistryEntry.EnchantmentCost maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(25, 8);
    private int anvilCost = 1;
    private EquipmentSlotGroup activeSlots = EquipmentSlotGroup.ANY;

    private List<TagKey<Enchantment>> injectInto = new LinkedList<>();

    protected final List<@NotNull GenerationContext> generationContexts = new LinkedList<>();
    @Nullable
    private EnchantmentType type;

    public EnchantmentDefinition(@NotNull Key key, @NotNull Component displayName) {
        super(key);
        this.displayName = displayName;
    }

    public final @NotNull Component description() {
        String descKey = "enchantment." + key().namespace() + "." + key().value() + ".desc";
        if (description == null) {
            return Component.translatable(descKey, "N/A");
        }
        if (description instanceof TextComponent text) {
            return Component.translatable(descKey, text.content()).mergeStyle(text);
        }

        return description.replaceText(
                TextReplacementConfig.builder()
                        .match("%max_level%")
                        .replacement(String.valueOf(maxLevel))
                        .build()
        );
    }

    public EnchantmentDefinition setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    public EnchantmentDefinition displayName(@NotNull Component displayName) {
        this.displayName = displayName;
        return this;
    }

    public EnchantmentDefinition description(@NotNull String description) {
        return description(Component.text(description));
    }
    public EnchantmentDefinition description(@Nullable Component description) {
        this.description = description;
        return this;
    }

    public EnchantmentDefinition targetDescription(@NotNull String targetDescription) {
        return targetDescription(Component.text(targetDescription));
    }
    public EnchantmentDefinition targetDescription(Component targetDescription) {
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
        displayName = section.getRichMessage("display_name", displayName);
        description = section.getRichMessage("description");

        parseRegistryKeys(RegistryKey.ITEM,
                section,
                "supported_items",
                directory,
                this::supportedItems,
                this::supportedItems);

        parseRegistryKeys(RegistryKey.ITEM,
                section,
                "primary_items",
                directory,
                this::primaryItems,
                this::primaryItems);

        int minBaseCost = section.getInt("minimum_cost.base_cost", minimumCost.baseCost());
        int minExtraCost = section.getInt("minimum_cost.additional_per_level_cost", minimumCost.additionalPerLevelCost());

        minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(minBaseCost, minExtraCost);

        int maxBaseCost = section.getInt("maximum_cost.base_cost", maximumCost.baseCost());
        int maxExtraCost = section.getInt("maximum_cost.additional_per_level_cost", maximumCost.additionalPerLevelCost());

        maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(maxBaseCost, maxExtraCost);

        parseRegistryKeys(RegistryKey.ENCHANTMENT,
                section,
                "exclusive_with",
                directory,
                this::exclusiveWith,
                this::exclusiveWith);

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
        if (typeKey != null) {
            type = EnchantmentTypeManager.getType(typeKey, type);
        }
    }

    private <T extends org.bukkit.Keyed> void parseRegistryKeys(RegistryKey<T> registryKey,
                                                                ConfigurationSection section,
                                                                String configKey,
                                                                String directory,
                                                                Consumer<RegistryKeySet<@NotNull T>> consumeKeySet,
                                                                Consumer<TagKey<T>> consumeTagKey
    ) {
        NamespacedKey namespacedKey;
        if (section.isList(configKey)) {
            List<TypedKey<T>> keys = new LinkedList<>();
            for (String entry : section.getStringList(configKey)) {
                namespacedKey = parseKey(entry, directory);
                keys.add(TypedKey.create(registryKey, namespacedKey));
            }
            consumeKeySet.accept(RegistrySet.keySet(registryKey, keys));
        } else {
            namespacedKey = parseKey(section, configKey, directory);
            if (namespacedKey != null) {
                consumeTagKey.accept(TagKey.create(registryKey, namespacedKey));
            }
        }
    }

    private void configureFallback(EnchantmentRegistryEntry.@Nullable Builder fallback) {
        if (fallback != null) {
            // Load defaults
            displayName = fallback.description();

            RegistryKeySet<@NotNull ItemType> typedKeys = fallback.primaryItems();
            if (typedKeys != null) {
                TagKey<ItemType> foundTagKey = tryGetTagName(typedKeys);
                if (foundTagKey != null) {
                    primaryItems = new TaggableItemKeySet(foundTagKey);
                } else {
                    primaryItems = new TaggableItemKeySet(typedKeys);
                }
            }

            typedKeys = fallback.supportedItems();
            // TODO: Migrate this method into the Taggable class
            TagKey<ItemType> foundTagKey = tryGetTagName(typedKeys);
            if (foundTagKey != null) {
                supportedItems = new TaggableItemKeySet(foundTagKey);
            } else {
                supportedItems = new TaggableItemKeySet(typedKeys);
            }

            RegistryKeySet<@NotNull Enchantment> typedEnchKeys = fallback.exclusiveWith();
            TagKey<Enchantment> foundEnchTagKey = tryGetTagName(typedEnchKeys);
            if (foundEnchTagKey != null) {
                exclusiveWith = new TaggableRegistryKeySet<>(foundEnchTagKey);
            } else {
                exclusiveWith = new TaggableRegistryKeySet<>(typedEnchKeys);
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

        // TODO: Find a way to allow missing entries when loading to use fallback default, so users can only
        //  override the details they want without locking in a specific version's details. Not loading everything
        //  by default is annoying because then they can't see existing details, but that locks in a version.

        section.set("enabled", isEnabled);
        section.set("display_name", MiniMessage.miniMessage().serialize(displayName));
        if (description != null) {
            section.set("description", MiniMessage.miniMessage().serialize(description));
        }

        if (supportedItems != null) {
            supportedItems.writeToConfig(section, "supported_items");
        }

        if (primaryItems != null) {
            primaryItems.writeToConfig(section, "primary_items");
        }

        if (exclusiveWith != null) {
            exclusiveWith.writeToConfig(section, "exclusive_with");
        }

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

                        throw new InvalidConfigurationException(ex.getMessage(), errorDir);
                    }
                } else {
                    throw new InvalidConfigurationException("Generation type must be a section: " + key, contextDir);
                }
            }
        }
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

    public Component getGenerationInfo(Component lineStart) {
        List<Component> methodComponents = deriveGenerationInfos();

        return lineStart.append(Component.join(
                JoinConfiguration.builder()
                        .separator(lineStart)
                        .parentStyle(Style.style(WbsEnchants.getInstance().getTextHighlightColour()))
                        .build(),
                methodComponents));
    }

    public boolean canGenerate() {
        return !deriveGenerationInfos().isEmpty();
    }

    protected @NotNull List<Component> deriveGenerationInfos() {
        List<Component> methodComponents = new LinkedList<>();

        if (isTagged(EnchantmentTagKeys.IN_ENCHANTING_TABLE)) {
            TextComponent lineStart = Component.text("   > ")
                    .color(WbsEnchants.getInstance().getTextHighlightColour());

            Component tableTargetDescription = null;

            if (primaryItems != null) {
                tableTargetDescription = primaryItems.getDisplay(lineStart);
            } else if (supportedItems != null) {
                tableTargetDescription = supportedItems.getDisplay(lineStart);
            }

            if (tableTargetDescription != null) {
                methodComponents.add(Component.text("Enchanting Table for: ").append(tableTargetDescription));
            }
        }

        if (isTagged(EnchantmentTagKeys.TRADEABLE)) {
            methodComponents.add(Component.text("Librarian trades"));
        }

        if (isTagged(EnchantmentTagKeys.ON_RANDOM_LOOT)) {
            methodComponents.add(Component.text("On any random loot"));
        }

        if (isTagged(EnchantmentTagKeys.ON_TRADED_EQUIPMENT)) {
            methodComponents.add(Component.text("On equipment traded by villagers"));
        }

        if (isTagged(EnchantmentTagKeys.ON_MOB_SPAWN_EQUIPMENT)) {
            methodComponents.add(Component.text("On mob spawn equipment"));
        }

        for (GenerationContext context : generationContexts) {
            methodComponents.add(context.getDescription());
        }
        return methodComponents;
    }

    public void buildTo(@NotNull TagProducer tagProducer,
                        EnchantmentRegistryEntry.Builder builder) {
        builder.description(displayName)
                .minimumCost(minimumCost)
                .maximumCost(maximumCost)
                .activeSlots(activeSlots)
                .anvilCost(anvilCost)
                .maxLevel(maxLevel())
                .weight(weight);

        if (supportedItems != null) {
            builder.supportedItems(supportedItems.getKeySet(tagProducer));
        }
        if (primaryItems != null) {
            builder.primaryItems(primaryItems.getKeySet(tagProducer));
        }
        if (exclusiveWith != null) {
            builder.exclusiveWith(exclusiveWith.getKeySet(tagProducer));
        }
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
    public Component getHoverText(@Nullable EnumSet<DescribeOptions> options) {
        // Get components without events -- events can't be used in hover text anyway
        List<Component> descriptionComponents = getDetailComponents(options, false);

        JoinConfiguration joinConfig = JoinConfiguration.builder()
                .separator(Component.text("\n"))
                .parentStyle(Style.style(WbsEnchants.getInstance().getTextColour()))
                .build();

        return Component.join(joinConfig, descriptionComponents);
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public @Nullable TaggableRegistryKeySet<ItemType> getPrimaryItems() {
        return primaryItems;
    }

    public EnchantmentDefinition primaryItems(TagKey<ItemType> primaryItems) {
        this.primaryItems = new TaggableItemKeySet(primaryItems);
        return this;
    }

    public EnchantmentDefinition primaryItems(RegistryKeySet<@NotNull ItemType> primaryItems) {
        this.primaryItems = new TaggableItemKeySet(primaryItems);
        return this;
    }

    public @Nullable TaggableRegistryKeySet<ItemType> getSupportedItems() {
        return supportedItems;
    }


    public EnchantmentDefinition supportedItems(TagKey<ItemType> supportedItems) {
        this.supportedItems = new TaggableItemKeySet(supportedItems);
        return this;
    }

    public EnchantmentDefinition supportedItems(RegistryKeySet<@NotNull ItemType> supportedItems) {
        this.supportedItems = new TaggableItemKeySet(supportedItems);
        return this;
    }

    public @Nullable TaggableRegistryKeySet<Enchantment> getExclusiveWith() {
        return exclusiveWith;
    }

    public EnchantmentDefinition exclusiveWith(TagKey<Enchantment> exclusiveWith) {
        this.exclusiveWith = new TaggableRegistryKeySet<>(exclusiveWith);
        return this;
    }

    public EnchantmentDefinition exclusiveWith(RegistryKeySet<@NotNull Enchantment> exclusiveWith) {
        this.exclusiveWith = new TaggableRegistryKeySet<>(exclusiveWith);
        return this;
    }

    public Component displayName() {
        return displayName.applyFallbackStyle(type().getColour());
    }

    public Component interactiveDisplay(EnumSet<DescribeOptions> options) {
        return displayName()
                .hoverEvent(
                        getHoverText(options).append(
                        Component.text("\n\nClick to view full info!")
                                .color(WbsEnchants.getInstance().getTextColour())
                        )
                ).clickEvent(ClickEvent.runCommand("/" +
                                WbsEnchants.getInstance().getName().toLowerCase()
                        + ":customenchants info " + key().asString()
                )
        );
    }

    @NotNull
    public EnchantmentType type() {
        if (type == null) {
            type = EnchantmentTypeManager.getType(getEnchantment());
        }
        return type;
    }

    /**
     * @return The enchantment type, which may be null if not explicitly set -- will not mutate the value like
     * {@link #type()} will.
     */
    @Nullable
    public EnchantmentType rawType() {
        return type;
    }

    public @NotNull Component targetDescription() {
        return getTargetDescription(
                Component.text("\n - ").color(WbsEnchants.getInstance().getTextHighlightColour())
        );
    }
    public @NotNull Component getTargetDescription(Component lineBreak) {
        if (targetDescription == null) {
            if (supportedItems != null) {
                return supportedItems.getDisplay(lineBreak);
            } else {
                return Component.text("Unknown");
            }
        }
        return targetDescription;
    }

    @NotNull
    public List<Component> getDetailComponents(boolean includeEvents) {
        return getDetailComponents(null, includeEvents);
    }
    @NotNull
    public List<Component> getDetailComponents(@Nullable EnumSet<DescribeOptions> options, boolean includeEvents) {
        if (options == null) {
            options = EnumSet.allOf(DescribeOptions.class);
        }

        List<Component> components = new LinkedList<>();

        int maxLevel = maxLevel();
        if (maxLevel == 0) {
            maxLevel = 1;
        }

        Style style = Style.style(WbsEnchants.getInstance().getTextColour());
        Style highlight = Style.style(WbsEnchants.getInstance().getTextHighlightColour());

        TextComponent lineStart = Component.text("\n - ").style(highlight);

        components.add(Component.text("Name: ").append(displayName())
                .append(Component.text(" (" + key().asString() + ")").color(NamedTextColor.GRAY)));

        if (options.contains(DescribeOptions.TYPE)) {
            components.add(Component.text("Type: ").append(type().getNameComponent()));
        }

        if (options.contains(DescribeOptions.MAX_LEVEL)) {
            components.add(Component.text("Maximum level: ").append(
                    Component.text(RomanNumerals.toRoman(maxLevel) + " (" + maxLevel + ")")
                            .style(highlight)
                    )
            );
        }

        if (options.contains(DescribeOptions.TARGET)) {
            components.add(Component.text("Target: ").append(
                    getTargetDescription(lineStart).style(highlight)
            ));
        }

        if (options.contains(DescribeOptions.DESCRIPTION)) {
            components.add(Component.text("Description: ").append(
                    description().style(highlight)));
        }

        if (options.contains(DescribeOptions.GENERATION)) {
            if (canGenerate()) {
                components.add(
                        Component.text("Generation:")
                                .append(
                                        getGenerationInfo(lineStart)
                                ).style(style)
                );
            }
        }

        if (options.contains(DescribeOptions.CONFLICTS)) {
            List<Enchantment> conflicts = EnchantUtils.getConflictsWith(getEnchantment());

            // Don't show enchants that only exist to conflict (typically curses)
            conflicts.removeIf(check -> EnchantUtils.getAsCustom(check) instanceof ConflictEnchantment);
            conflicts.removeIf(other -> key().equals(other.getKey()));

            if (!conflicts.isEmpty()) {
                Component conflictsComponent = Component.text("Conflicts with: ");

                // If this is a conflict enchantment, show that description instead.
                if (EnchantManager.getCustomFromKey(key()) instanceof ConflictEnchantment conflictEnchant) {
                    conflictsComponent = conflictsComponent.append(Component.text(conflictEnchant.getConflictsDescription()));
                } else if (!conflicts.isEmpty()) {
                    conflicts.sort(Comparator.comparing(org.bukkit.Keyed::getKey));

                    for (Enchantment conflict : conflicts) {
                        Component conflictComponent = EnchantUtils.getDisplayName(conflict);

                        if (includeEvents) {
                            Component hoverText = EnchantUtils.getHoverText(conflict);
                            // TODO: Check if this inherits parent's styling, if not, need to pass in default styling as param
                            hoverText = hoverText.append(Component.text("\n\nClick to view full info!"));

                            conflictsComponent = conflictsComponent
                                    .hoverEvent(hoverText)
                                    .clickEvent(ClickEvent.runCommand("/" +
                                            WbsEnchants.getInstance().getName().toLowerCase()
                                            + ":customenchants info " + key().asString()
                                    ));
                        }

                        conflictsComponent = conflictsComponent.append(lineStart)
                                .append(conflictComponent);
                    }

                }

                components.add(conflictsComponent);
            }
        }

        return components;
    }

    public int compareTo(EnchantmentDefinition other) {
        int typeComparison = type().compareTo(other.type());
        if (typeComparison != 0) {
            return typeComparison;
        }

        String stringName = this.tryGetStringName();
        String otherStringName = this.tryGetStringName();
         if (Objects.equals(stringName, otherStringName)) {
             return key().compareTo(other.key());
         }
         if (stringName == null) {
             return -1;
         } else if (otherStringName == null) {
             return 1;
         } else {
             return stringName.compareTo(otherStringName);
         }
    }

    private String tryGetStringName() {
        Component currentDisplayName = displayName();
        if (currentDisplayName instanceof TextComponent text) {
            return text.content();
        } else if (currentDisplayName instanceof TranslatableComponent translatable) {
            return translatable.fallback();
        }
        return MiniMessage.miniMessage().serialize(currentDisplayName.style(Style.empty()));
    }

    public interface TagProducer {
        @Contract("!null -> !null")
        <V extends org.bukkit.Keyed> Tag<@NotNull V> getOrCreateTag(TagKey<V> tagKey);
    }

    public enum DescribeOptions {
        MAX_LEVEL,
        TARGET,
        TYPE,
        GENERATION,
        CONFLICTS,
        DESCRIPTION
    }
}
