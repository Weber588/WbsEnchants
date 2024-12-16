package wbs.enchants;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import io.papermc.paper.tag.PreFlattenTagRegistrar;
import io.papermc.paper.tag.TagEntry;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;

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

    public static final TagKey<ItemType> SPONGES = ItemTypeTagKeys.create(createKey("sponges"));
    public static final TagKey<ItemType> MAPS = ItemTypeTagKeys.create(createKey("maps"));
    public static final TagKey<ItemType> MINECARTS = ItemTypeTagKeys.create(createKey("minecarts"));
    public static final TagKey<ItemType> ENCHANTABLE_HANDHELD = ItemTypeTagKeys.create(createKey("enchantable/handheld"));
    public static final TagKey<ItemType> ENCHANTABLE_VEHICLE = ItemTypeTagKeys.create(createKey("enchantable/vehicle"));
    public static final TagKey<ItemType> ENCHANTABLE_GROUND_MINING = ItemTypeTagKeys.create(createKey("enchantable/ground_mining"));
    public static final TagKey<ItemType> BELL = ItemTypeTagKeys.create(createKey("enchantable/bell"));
    public static final TagKey<ItemType> ELYTRA = ItemTypeTagKeys.create(createKey("enchantable/elytra"));
    public static final TagKey<ItemType> BUCKET = ItemTypeTagKeys.create(createKey("enchantable/bucket"));
    public static final TagKey<ItemType> SHIELD = ItemTypeTagKeys.create(createKey("enchantable/shield"));
    public static final TagKey<ItemType> ENCHANTABLE_PROJECTILE_WEAPON = ItemTypeTagKeys.create(createKey("enchantable/projectile_weapon"));
    public static final TagKey<ItemType> ENCHANTABLE_BUNDLE = ItemTypeTagKeys.create(createKey("enchantable/bundle"));

    // TODO: Put this somewhere proper (config?)
    private static Set<CustomTag<ItemType>> getItemTags() {
        return Set.of(
                new CustomTag<>(SPONGES, ItemType.SPONGE, ItemType.WET_SPONGE),
                new CustomTag<>(MAPS, ItemType.MAP, ItemType.FILLED_MAP),
                new CustomTag<>(MINECARTS,
                        ItemType.MINECART,
                        ItemType.CHEST_MINECART,
                        ItemType.COMMAND_BLOCK_MINECART,
                        ItemType.FURNACE_MINECART,
                        ItemType.HOPPER_MINECART,
                        ItemType.TNT_MINECART
                ),
                new CustomTag<>(ENCHANTABLE_HANDHELD,
                        ItemType.CRAFTING_TABLE,
                        ItemType.ENCHANTING_TABLE,
                        ItemType.ENDER_CHEST,
                        ItemType.CARTOGRAPHY_TABLE,
                        ItemType.STONECUTTER,
                        ItemType.GRINDSTONE,
                        ItemType.LOOM,
                        ItemType.SMITHING_TABLE
                ),
                new CustomTag<>(ENCHANTABLE_VEHICLE,
                        Set.of(ItemType.MINECART),
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
                new CustomTag<>(BELL,
                        ItemType.MINECART
                ),
                new CustomTag<>(ELYTRA,
                        ItemType.ELYTRA
                ),
                new CustomTag<>(BUCKET,
                        ItemType.BUCKET
                ),
                new CustomTag<>(SHIELD,
                        ItemType.SHIELD
                ),
                new CustomTag<>(ENCHANTABLE_PROJECTILE_WEAPON,
                        ItemType.BOW,
                        ItemType.CROSSBOW
                ),
                new CustomTag<>(ENCHANTABLE_BUNDLE,
                        ItemType.BUNDLE
                )
        );
    }

    public static final TagKey<Enchantment> EXCLUSIVE_SET_MULTIMINER = EnchantmentTagKeys.create(createKey("exclusive_set/multiminer"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_SELF_REPAIRING = EnchantmentTagKeys.create(createKey("exclusive_set/self_repairing"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_FALL_DAMAGE_AFFECTING = EnchantmentTagKeys.create(createKey("exclusive_set/fall_damage_affecting"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_DEFUSAL = EnchantmentTagKeys.create(createKey("exclusive_set/defusal"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_MIDAS = EnchantmentTagKeys.create(createKey("exclusive_set/midas"));
    public static final TagKey<Enchantment> EXCLUSIVE_SET_MAPS = EnchantmentTagKeys.create(createKey("exclusive_set/maps"));
    public static final TagKey<Enchantment> VANILLA = EnchantmentTagKeys.create(createKey("vanilla"));
    public static final TagKey<Enchantment> CUSTOM = EnchantmentTagKeys.create(createKey("custom"));

    private static Set<CustomTag<Enchantment>> getEnchantmentTags() {
        return Set.of(
                CustomTag.getKeyTag(EXCLUSIVE_SET_SELF_REPAIRING,
                        TypedKey.create(RegistryKey.ENCHANTMENT, NamespacedKey.minecraft("mending"))
                     //   TypedKey.create(RegistryKey.ENCHANTMENT, new NamespacedKey("nova_structures", "photosynthesis"))
                ),
                CustomTag.getKeyTag(EXCLUSIVE_SET_FALL_DAMAGE_AFFECTING,
                        TypedKey.create(RegistryKey.ENCHANTMENT, NamespacedKey.minecraft("feather_falling"))
                ),
                CustomTag.getKeyTag(EXCLUSIVE_SET_DEFUSAL,
                        TypedKey.create(RegistryKey.ENCHANTMENT, NamespacedKey.minecraft("knockback"))
                ),
                CustomTag.getKeyTag(EXCLUSIVE_SET_MIDAS,
                        TypedKey.create(RegistryKey.ENCHANTMENT, NamespacedKey.minecraft("silk_touch"))
                )
        );
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new WbsEnchants();
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        LifecycleEventManager<@NotNull BootstrapContext> manager = context.getLifecycleManager();

        Multimap<EnchantmentType, TypedKey<Enchantment>> typeEnchantments = HashMultimap.create();

        // Register a new handled for the freeze lifecycle event on the enchantment registry
        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
            for (WbsEnchantment enchant : EnchantManager.getRegistered()) {
                event.registry().register(
                        TypedKey.create(RegistryKey.ENCHANTMENT, enchant.getKey()),
                        builder -> enchant.buildTo(event, builder)
                );
            }
        }));

        for (WbsEnchantment enchant : EnchantManager.getRegistered()) {
            typeEnchantments.put(enchant.getType(), enchant.getTypedKey());
        }

        Set<CustomTag<Enchantment>> dynamicEnchantmentTags = new HashSet<>();

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

            dynamicEnchantmentTags.add(typeTag);
        }

        CustomTag<Enchantment> vanillaTag = new CustomTag<>(VANILLA);
        vanillaTag.typedKeys = new LinkedList<>();
        CustomTag<Enchantment> customTag = new CustomTag<>(CUSTOM);
        customTag.typedKeys = new LinkedList<>();

        manager.registerEventHandler(RegistryEvents.ENCHANTMENT.entryAdd().newHandler(event -> {
            TypedKey<Enchantment> key = event.key();
            if (key.key().namespace().equalsIgnoreCase("minecraft")) {
                vanillaTag.typedKeys.add(key);
            } else {
                customTag.typedKeys.add(key);
            }
        }));

        dynamicEnchantmentTags.add(customTag);
        dynamicEnchantmentTags.add(vanillaTag);

        registerCustomTags(context, RegistryKey.ENCHANTMENT, () -> dynamicEnchantmentTags);

        registerCustomTags(context, RegistryKey.ITEM, WbsEnchantsBootstrap::getItemTags);
        registerCustomTags(context, RegistryKey.ENCHANTMENT, WbsEnchantsBootstrap::getEnchantmentTags);

        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
            Multimap<TagKey<Enchantment>, TypedKey<Enchantment>> toAdd = HashMultimap.create();
            for (WbsEnchantment enchant : EnchantManager.getRegistered()) {
                for (TagKey<Enchantment> tag : enchant.addToTags()) {
                    toAdd.put(tag, enchant.getTypedKey());
                }
            }

            for (TagKey<Enchantment> tag : toAdd.keys()) {
                event.registrar().addToTag(tag, toAdd.get(tag));
            }
        }));
    }

    private static <T extends Keyed> void registerCustomTags(@NotNull BootstrapContext context,
                                                             RegistryKey<T> key,
                                                             Supplier<Set<CustomTag<T>>> tags) {
        LifecycleEventManager<@NotNull BootstrapContext> manager = context.getLifecycleManager();

        manager.registerEventHandler(LifecycleEvents.TAGS.preFlatten(key).newHandler(event -> {
            tags.get().forEach(tag ->
                            tag.register(event.registrar())
            );
        }));
        manager.registerEventHandler(LifecycleEvents.TAGS.postFlatten(key).newHandler(event -> {
            tags.get().forEach(tag ->
                    tag.register(event.registrar())
            );
        }));
    }

    private static class CustomTag<T extends Keyed> {
        private final TagKey<T> key;
        private Collection<T> values;
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


        @SafeVarargs
        private CustomTag(TagKey<T> key, T ... values) {
            this(key, Arrays.asList(values));
        }

        private CustomTag(TagKey<T> key, Collection<T> values) {
            this(key);
            this.values = values;
        }

        private CustomTag(TagKey<T> key, Collection<T> values, Collection<TagEntry<T>> tagEntries) {
            this(key, values);
            this.tagEntries = tagEntries;
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

            Collection<TypedKey<T>> values;
            if (typedKeys != null) {
                values = typedKeys;
            } else {
                values = this.values.stream()
                        .map(type -> TypedKey.create(registryKey, type.key()))
                        .toList();
            }

            if (registrar.hasTag(key)) {
                registrar.addToTag(key, values);
            } else {
                registrar.setTag(key, values);
            }
        }
    }
}