package wbs.enchants;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import io.papermc.paper.tag.PreFlattenTagRegistrar;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.WbsFileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class WbsEnchantsBootstrap implements PluginBootstrap {
    public static final String NAMESPACE = "wbsenchants";

    public static NamespacedKey createKey(String value) {
        return new NamespacedKey(NAMESPACE, value);
    }
    
    public static final TagKey<ItemType> ITEM_EMPTY =
            TagKey.create(RegistryKey.ITEM, new NamespacedKey(NAMESPACE, "empty"));
    public static final TagKey<Enchantment> ENCHANTMENT_EMPTY =
            TagKey.create(RegistryKey.ENCHANTMENT, new NamespacedKey(NAMESPACE, "empty"));

    public static final TagKey<ItemType> IRON_TOOLS = ItemTypeTagKeys.create(createKey("tools/iron"));
    public static final TagKey<ItemType> IRON_ARMOR = ItemTypeTagKeys.create(createKey("armor/iron"));
    public static final TagKey<ItemType> CHAINMAIL_ARMOR = ItemTypeTagKeys.create(createKey("armor/chainmail"));

    public static final TagKey<ItemType> SPONGES = ItemTypeTagKeys.create(createKey("sponges"));
    public static final TagKey<ItemType> MAPS = ItemTypeTagKeys.create(createKey("maps"));
    public static final TagKey<ItemType> MINECARTS = ItemTypeTagKeys.create(createKey("minecarts"));
    public static final TagKey<ItemType> ENCHANTABLE_HANDHELD = ItemTypeTagKeys.create(createKey("enchantable/handheld"));
    public static final TagKey<ItemType> ENCHANTABLE_VEHICLE = ItemTypeTagKeys.create(createKey("enchantable/vehicle"));
    public static final TagKey<ItemType> ENCHANTABLE_GROUND_MINING = ItemTypeTagKeys.create(createKey("enchantable/ground_mining"));
    public static final TagKey<ItemType> ENCHANTABLE_PROJECTILE_WEAPON = ItemTypeTagKeys.create(createKey("enchantable/projectile_weapon"));
    public static final TagKey<ItemType> ENCHANTABLE_SHULKER_BOX = ItemTypeTagKeys.create(createKey("enchantable/shulker_box"));
    public static final TagKey<ItemType> ENCHANTABLE_RUSTABLE = ItemTypeTagKeys.create(createKey("enchantable/rustable"));
    public static final TagKey<ItemType> ENCHANTABLE_MAGNETIC_ARMOR = ItemTypeTagKeys.create(createKey("enchantable/magnetic_armor"));

    // Items that are designed to create loot -- mining tools (block drops), weapons (mob drops)
    public static final TagKey<ItemType> ENCHANTABLE_LOOT_CREATORS = ItemTypeTagKeys.create(createKey("enchantable/loot_creators"));

    public static final TagKey<ItemType> ENCHANTABLE_BUNDLE = ItemTypeTagKeys.create(createKey("enchantable/bundle"));
    public static final TagKey<ItemType> ENCHANTABLE_BEACON = ItemTypeTagKeys.create(createKey("enchantable/beacon"));
    public static final TagKey<ItemType> ENCHANTABLE_ELYTRA = ItemTypeTagKeys.create(createKey("enchantable/elytra"));
    public static final TagKey<ItemType> ENCHANTABLE_BUCKET = ItemTypeTagKeys.create(createKey("enchantable/bucket"));

    // TODO: Put this somewhere proper (config?)
    private static Set<CustomTag<ItemType>> getItemTags() {
        return Set.of(
                new CustomTag<>(SPONGES, ItemTypeKeys.SPONGE, ItemTypeKeys.WET_SPONGE),
                new CustomTag<>(MAPS, ItemTypeKeys.MAP, ItemTypeKeys.FILLED_MAP),
                new CustomTag<>(MINECARTS,
                        ItemTypeKeys.MINECART,
                        ItemTypeKeys.CHEST_MINECART,
                        ItemTypeKeys.COMMAND_BLOCK_MINECART,
                        ItemTypeKeys.FURNACE_MINECART,
                        ItemTypeKeys.HOPPER_MINECART,
                        ItemTypeKeys.TNT_MINECART
                ),
                new CustomTag<>(ENCHANTABLE_HANDHELD,
                        ItemTypeKeys.CRAFTING_TABLE,
                        ItemTypeKeys.ENCHANTING_TABLE,
                        ItemTypeKeys.ENDER_CHEST,
                        ItemTypeKeys.CARTOGRAPHY_TABLE,
                        ItemTypeKeys.STONECUTTER,
                        ItemTypeKeys.GRINDSTONE,
                        ItemTypeKeys.LOOM,
                        ItemTypeKeys.SMITHING_TABLE
                ),
                new CustomTag<>(ENCHANTABLE_VEHICLE,
                        Set.of(ItemTypeKeys.MINECART),
                        Set.of(
                                TagEntry.tagEntry(ItemTypeTagKeys.BOATS, true),
                                TagEntry.tagEntry(ItemTypeTagKeys.CHEST_BOATS, true)
                        )
                ),
                new CustomTag<>(ENCHANTABLE_GROUND_MINING,
                        Set.of(),
                        Set.of(
                                TagEntry.tagEntry(ItemTypeTagKeys.PICKAXES, true),
                                TagEntry.tagEntry(ItemTypeTagKeys.SHOVELS, true)
                        )
                ),
                new CustomTag<>(ENCHANTABLE_SHULKER_BOX,
                        ItemTypeKeys.SHULKER_BOX,
                        ItemTypeKeys.WHITE_SHULKER_BOX,
                        ItemTypeKeys.ORANGE_SHULKER_BOX,
                        ItemTypeKeys.MAGENTA_SHULKER_BOX,
                        ItemTypeKeys.LIGHT_BLUE_SHULKER_BOX,
                        ItemTypeKeys.YELLOW_SHULKER_BOX,
                        ItemTypeKeys.LIME_SHULKER_BOX,
                        ItemTypeKeys.PINK_SHULKER_BOX,
                        ItemTypeKeys.GRAY_SHULKER_BOX,
                        ItemTypeKeys.LIGHT_GRAY_SHULKER_BOX,
                        ItemTypeKeys.CYAN_SHULKER_BOX,
                        ItemTypeKeys.PURPLE_SHULKER_BOX,
                        ItemTypeKeys.BLUE_SHULKER_BOX,
                        ItemTypeKeys.BROWN_SHULKER_BOX,
                        ItemTypeKeys.GREEN_SHULKER_BOX,
                        ItemTypeKeys.RED_SHULKER_BOX,
                        ItemTypeKeys.BLACK_SHULKER_BOX
                ),
                new CustomTag<>(ENCHANTABLE_PROJECTILE_WEAPON,
                        ItemTypeKeys.BOW,
                        ItemTypeKeys.CROSSBOW
                ),
                new CustomTag<>(IRON_TOOLS,
                        ItemTypeKeys.IRON_PICKAXE,
                        ItemTypeKeys.IRON_SHOVEL,
                        ItemTypeKeys.IRON_AXE,
                        ItemTypeKeys.IRON_SWORD,
                        ItemTypeKeys.IRON_HOE
                ),
                new CustomTag<>(IRON_ARMOR,
                        ItemTypeKeys.IRON_HELMET,
                        ItemTypeKeys.IRON_CHESTPLATE,
                        ItemTypeKeys.IRON_LEGGINGS,
                        ItemTypeKeys.IRON_BOOTS
                ),
                new CustomTag<>(CHAINMAIL_ARMOR,
                        ItemTypeKeys.CHAINMAIL_HELMET,
                        ItemTypeKeys.CHAINMAIL_CHESTPLATE,
                        ItemTypeKeys.CHAINMAIL_LEGGINGS,
                        ItemTypeKeys.CHAINMAIL_BOOTS
                ),
                new CustomTag<>(ENCHANTABLE_RUSTABLE,
                        Set.of(
                                ItemTypeKeys.SHEARS,
                                ItemTypeKeys.FLINT_AND_STEEL
                        ),
                        Set.of(
                                TagEntry.tagEntry(IRON_TOOLS, true),
                                TagEntry.tagEntry(IRON_ARMOR, true),
                                TagEntry.tagEntry(CHAINMAIL_ARMOR, true)
                        )
                ).priority(1),
                new CustomTag<>(ENCHANTABLE_MAGNETIC_ARMOR,
                        Set.of(),
                        Set.of(
                                TagEntry.tagEntry(IRON_ARMOR, true),
                                TagEntry.tagEntry(CHAINMAIL_ARMOR, true)
                        )
                ).priority(1),
                new CustomTag<>(ENCHANTABLE_LOOT_CREATORS,
                        Set.of(),
                        Set.of(
                                TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_MINING, true),
                                TagEntry.tagEntry(ItemTypeTagKeys.ENCHANTABLE_WEAPON, true)
                        )
                ),
                new CustomTag<>(ENCHANTABLE_BUNDLE,
                        Set.of(ItemTypeKeys.BUNDLE),
                        Set.of()
                ),
                new CustomTag<>(ENCHANTABLE_BEACON,
                        Set.of(ItemTypeKeys.BEACON),
                        Set.of()
                ),
                new CustomTag<>(ENCHANTABLE_ELYTRA,
                        Set.of(ItemTypeKeys.ELYTRA),
                        Set.of()
                ),
                new CustomTag<>(ENCHANTABLE_BUCKET,
                        Set.of(
                                ItemTypeKeys.BUCKET,
                                ItemTypeKeys.AXOLOTL_BUCKET,
                                ItemTypeKeys.COD_BUCKET,
                                ItemTypeKeys.LAVA_BUCKET,
                                ItemTypeKeys.MILK_BUCKET,
                                ItemTypeKeys.POWDER_SNOW_BUCKET,
                                ItemTypeKeys.PUFFERFISH_BUCKET,
                                ItemTypeKeys.SALMON_BUCKET,
                                ItemTypeKeys.TADPOLE_BUCKET,
                                ItemTypeKeys.TROPICAL_FISH_BUCKET,
                                ItemTypeKeys.WATER_BUCKET
                        ),
                        Set.of()
                )
        );
    }

    public static final TagKey<Enchantment> EXCLUSIVE_SET_MULTIMINER = EnchantmentTagKeys.create(createKey("exclusive_set/multiminer"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_SELF_REPAIRING = EnchantmentTagKeys.create(createKey("exclusive_set/self_repairing"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_FALL_DAMAGE_AFFECTING = EnchantmentTagKeys.create(createKey("exclusive_set/fall_damage_affecting"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_DEFUSAL = EnchantmentTagKeys.create(createKey("exclusive_set/defusal"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_MIDAS = EnchantmentTagKeys.create(createKey("exclusive_set/midas"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_MAPS = EnchantmentTagKeys.create(createKey("exclusive_set/maps"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_ARMOR_RETALIATION = EnchantmentTagKeys.create(createKey("exclusive_set/armor_retaliation"));
    public static final TagKey<Enchantment> VANILLA = EnchantmentTagKeys.create(createKey("vanilla"));
    public static final TagKey<Enchantment> CUSTOM = EnchantmentTagKeys.create(createKey("custom"));

    private static Set<CustomTag<Enchantment>> getEnchantmentTags() {
        return Set.of(
                CustomTag.getKeyTag(EXCLUSIVE_SET_SELF_REPAIRING,
                        EnchantmentKeys.MENDING
                     //   TypedKey.create(RegistryKey.ENCHANTMENT, new NamespacedKey("nova_structures", "photosynthesis"))
                ),
                CustomTag.getKeyTag(EXCLUSIVE_SET_FALL_DAMAGE_AFFECTING,
                        EnchantmentKeys.FEATHER_FALLING
                ),
                CustomTag.getKeyTag(EXCLUSIVE_SET_DEFUSAL,
                        EnchantmentKeys.KNOCKBACK
                ),
                CustomTag.getKeyTag(EXCLUSIVE_SET_MIDAS,
                        EnchantmentKeys.SILK_TOUCH
                ),
                CustomTag.getKeyTag(EXCLUSIVE_SET_ARMOR_RETALIATION,
                        EnchantmentKeys.THORNS
                )
        );
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        if (newExternalEnchants) {
            try {
                externalEnchantsConfig.save(externalEnchantsFile);
            } catch (IOException ex) {
                context.getLogger().error(ex.getMessage());
            }
        }
        return new WbsEnchants();
    }


    protected YamlConfiguration loadConfigSafely(File file, ComponentLogger logger) {
        Objects.requireNonNull(file, "File cannot be null");
        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ex) {
            logger.error("File not found: {}", file, ex);
        } catch (IOException ex) {
            logger.error("Cannot load {}", file, ex);
        } catch (InvalidConfigurationException ex) {
            logger.error("Cannot load {}", file, ex);
            logger.error("&cYAML parsing error in file {}. See console for details.", file.getName());
        }

        return config;
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        LifecycleEventManager<@NotNull BootstrapContext> manager = context.getLifecycleManager();

        registerEnchantmentEvents(context, manager);

        registerCustomTags(context, manager);

        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
            Multimap<TagKey<Enchantment>, TypedKey<Enchantment>> toAdd = HashMultimap.create();

            // Tag events (currently) run after registry events for the same type, so at when this runs, all definitions should
            // be loaded.
            for (EnchantmentDefinition definition : EnchantManager.getAllKnownDefinitions()) {
                for (TagKey<Enchantment> tag : definition.injectInto()) {
                    toAdd.put(tag, definition.getTypedKey());
                }

                // Can't derive type yet -- get raw type.
                EnchantmentType type = definition.rawType();
                if (type != null) {
                    TagKey<Enchantment> typeTagKey = type.getTagKey();
                    if (typeTagKey != null) {
                        toAdd.put(typeTagKey, definition.getTypedKey());
                    }
                }

                if (EnchantManager.isManaged(definition)) {
                    if (definition.getPrimaryItems() != null && !definition.getPrimaryItems().isEmpty()) {
                        toAdd.put(EnchantmentTagKeys.IN_ENCHANTING_TABLE, definition.getTypedKey());
                    }
                }
            }

            for (TagKey<Enchantment> tag : toAdd.keys()) {
                event.registrar().addToTag(tag, toAdd.get(tag));
            }
        }));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void writeJSONToFile(Path folder, NamespacedKey key, Gson gson, Object object) {
    }

    private static void registerCustomTags(@NotNull BootstrapContext context, LifecycleEventManager<@NotNull BootstrapContext> manager) {
        registerCustomTags(context, RegistryKey.ITEM, WbsEnchantsBootstrap::getItemTags);
        registerCustomTags(context, RegistryKey.ENCHANTMENT, WbsEnchantsBootstrap::getEnchantmentTags);

        CustomTag<Enchantment> vanillaTag = new CustomTag<>(VANILLA);
        vanillaTag.typedKeys = new LinkedList<>();
        CustomTag<Enchantment> customTag = new CustomTag<>(CUSTOM);
        customTag.typedKeys = new LinkedList<>();

        // Entry add registry phase occurs before tag registry events fire -- so this will populate
        // before the events scheduled below, even though at runtime of the encompassing method, typedKeys
        // remains empty.
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
            TypedKey<Enchantment> key = event.key();
            if (key.key().namespace().equalsIgnoreCase("minecraft")) {
                vanillaTag.typedKeys.add(key);
            } else {
                customTag.typedKeys.add(key);
            }
        }));

        Set<CustomTag<Enchantment>> dynamicEnchantmentTags = new HashSet<>();
        dynamicEnchantmentTags.add(customTag);
        dynamicEnchantmentTags.add(vanillaTag);

        registerCustomTags(context, RegistryKey.ENCHANTMENT, () -> dynamicEnchantmentTags);
    }

    private static @NotNull Set<CustomTag<Enchantment>> getEnchantmentTypeTags() {
        Multimap<EnchantmentType, TypedKey<Enchantment>> typeEnchantments = HashMultimap.create();

        for (EnchantmentDefinition definition : EnchantManager.getAllKnownDefinitions()) {
            typeEnchantments.put(definition.rawType(), definition.getTypedKey());
        }

        Set<CustomTag<Enchantment>> enchantmentTypeTags = new HashSet<>();
        for (EnchantmentType type : EnchantmentTypeManager.getRegistered()) {
            Collection<TypedKey<Enchantment>> enchantmentKeys = typeEnchantments.get(type);
            TagKey<Enchantment> tagKey = type.getTagKey();

            // Don't create a tag for Regular.
            // Instead, we'll determine if an enchant is "regular" by checking all other types first.
            if (tagKey == null) {
                continue;
            }

            CustomTag<Enchantment> typeTag = new CustomTag<>(tagKey);
            typeTag.typedKeys = new LinkedList<>();

            typeTag.typedKeys.addAll(enchantmentKeys);

            enchantmentTypeTags.add(typeTag);
        }
        return enchantmentTypeTags;
    }

    private void registerEnchantmentEvents(@NotNull BootstrapContext context, LifecycleEventManager<@NotNull BootstrapContext> manager) {
        File dataDirectory = context.getDataDirectory().toFile();
        if (!dataDirectory.exists()) {
            if (!dataDirectory.mkdir()) {
                context.getLogger().error("Failed to generate data directory!");
            }
        }

        configureExternalEnchantments(context, manager);
        registerCustomEnchantments(context, manager);
    }

    private File externalEnchantsFile;
    private YamlConfiguration externalEnchantsConfig;
    private boolean newExternalEnchants = false;

    private void configureExternalEnchantments(@NotNull BootstrapContext context, LifecycleEventManager<@NotNull BootstrapContext> manager) {
        String fileName = "external_enchantments.yml";

        externalEnchantsFile = new File(context.getDataDirectory().toFile(), fileName);
        YamlConfiguration enchantsConfig;
        if (!externalEnchantsFile.exists()) {
            WbsFileUtil.saveResource(context, WbsEnchants.class, fileName, false);
        }
        externalEnchantsConfig = loadConfigSafely(externalEnchantsFile, context.getLogger());

        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
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

    private void registerCustomEnchantments(@NotNull BootstrapContext context, LifecycleEventManager<@NotNull BootstrapContext> manager) {
        File customEnchantsFile = new File(context.getDataDirectory().toFile(), "enchantments.yml");
        YamlConfiguration enchantsConfig;
        if (customEnchantsFile.exists()) {
            enchantsConfig = loadConfigSafely(customEnchantsFile, context.getLogger());
        } else {
            enchantsConfig = null;
        }

        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
            List<WbsEnchantment> invalidEnchantments = new LinkedList<>();
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

                event.registry().register(
                        TypedKey.create(RegistryKey.ENCHANTMENT, enchant.key()),
                        builder -> enchant.getDefinition().buildTo(event::getOrCreateTag, builder)
                );
            }

            if (!invalidEnchantments.isEmpty()) {
                invalidEnchantments.forEach(EnchantManager::unregister);
                context.getLogger().error("Unregistered {} invalid enchantments.", invalidEnchantments.size());
            }
        }));
    }

    private static <T extends Keyed> void registerCustomTags(@NotNull BootstrapContext context,
                                                             RegistryKey<T> key,
                                                             Supplier<Set<CustomTag<T>>> tags) {
        LifecycleEventManager<@NotNull BootstrapContext> manager = context.getLifecycleManager();

        manager.registerEventHandler(LifecycleEvents.TAGS.preFlatten(key).newHandler(event ->
                tags.get().stream().sorted(Comparator.comparingInt(CustomTag::priority)).forEach(tag ->
                        tag.register(event.registrar())
                )
        ));
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(key).newHandler(event ->
                tags.get().stream().sorted(Comparator.comparingInt(CustomTag::priority)).forEach(tag ->
                        tag.register(event.registrar())
                )
        ));
    }

    private static class CustomTag<T extends Keyed> {
        private final TagKey<T> key;
        private int priority = 0;
        private Collection<TypedKey<T>> typedKeys;
        private Collection<TagEntry<T>> tagEntries;

        private static CustomTag<Enchantment> getKeyTag(TagKey<Enchantment> key, WbsEnchantment ... enchants) {
            CustomTag<Enchantment> tag = new CustomTag<>(key);

            tag.typedKeys = Arrays.stream(enchants).map(WbsEnchantment::getTypedKey).toList();

            return tag;
        }

        @SafeVarargs
        private static <T extends Keyed> CustomTag<T> getKeyTag(TagKey<T> key, TypedKey<T> ... keys) {
            CustomTag<T> tag = new CustomTag<>(key);

            tag.typedKeys = List.of(keys);

            return tag;
        }

        private CustomTag(TagKey<T> key) {
            this.key = key;
        }

        private CustomTag(TagKey<T> key, Collection<TypedKey<T>> keys) {
            this.key = key;
            this.typedKeys = keys;
        }

        @SafeVarargs
        private CustomTag(TagKey<T> key, TypedKey<T> ... keys) {
            this(key, Arrays.asList(keys));
        }

        private CustomTag(TagKey<T> key, Collection<TypedKey<T>> keys, Collection<TagEntry<T>> tagEntries) {
            this.key = key;
            this.typedKeys = keys;
            this.tagEntries = tagEntries;
        }

        public int priority() {
            return priority;
        }

        public CustomTag<T> priority(int priority) {
            this.priority = priority;
            return this;
        }

        private void register(PreFlattenTagRegistrar<T> registrar) {
            if (tagEntries == null || tagEntries.isEmpty()) {
                return;
            }

            if (registrar.hasTag(key)) {
                registrar.addToTag(key, tagEntries);
            } else {
                registrar.setTag(key, tagEntries);
            }
        }

        private void register(PostFlattenTagRegistrar<T> registrar) {
            RegistryKey<T> registryKey = registrar.registryKey();

            if (registrar.hasTag(key)) {
                registrar.addToTag(key, typedKeys);
            } else {
                registrar.setTag(key, typedKeys);
            }
        }
    }
}