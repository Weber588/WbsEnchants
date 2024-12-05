package wbs.enchants;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryFreezeEvent;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.enchantment.helper.*;
import wbs.enchants.generation.ContextManager;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.string.RomanNumerals;
import wbs.utils.util.string.WbsStrings;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public abstract class WbsEnchantment implements Comparable<WbsEnchantment>, Keyed, Listener {
    private final String stringKey;

    protected boolean isEnabled = true;
    @NotNull
    protected List<String> aliases = new LinkedList<>();

    protected int maxLevel = 1;
    protected int weight = 1;
    // Defaulting to Smite levels; feels the most middling
    protected EnchantmentRegistryEntry.EnchantmentCost maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(25, 8);
    protected EnchantmentRegistryEntry.EnchantmentCost minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(5, 8);
    protected TagKey<ItemType> primaryItems;
    protected TagKey<ItemType> supportedItems;
    protected TagKey<Enchantment> exclusiveWith;
    protected int anvilCost;
    @NotNull
    protected String description;
    protected String targetDescription;
    private String displayName;

    protected final List<GenerationContext> generationContexts = new LinkedList<>();

    public WbsEnchantment(String key, @NotNull String description) {
        stringKey = key;
        this.description = description;
        EnchantManager.register(this);
    }

    public EnchantmentType getType() {
        return EnchantmentTypeManager.REGULAR;
    }

    @NotNull
    public final String getDescription() {
        return description
                .replaceAll("%max_level%", String.valueOf(getMaxLevel()));
    }
    @NotNull
    public String getTargetDescription() {
        if (targetDescription == null) {
            targetDescription = getSupportedItems().key().value();
            targetDescription = targetDescription.substring(targetDescription.lastIndexOf('/') + 1)
                    .replaceAll("_", " ");

            targetDescription = WbsStrings.capitalizeAll(targetDescription);
        }
        return targetDescription;
    }

    public String getPermission() {
        return "wbsenchants.enchantment." + getKey().getNamespace() + "." + getKey().getKey();
    }

    @NotNull
    public List<String> getAliases() {
        return aliases;
    }

    @NotNull
    public NamespacedKey getKey() {
        return new NamespacedKey("wbsenchants", stringKey);
    }

    public TypedKey<Enchantment> getTypedKey() {
        return TypedKey.create(RegistryKey.ENCHANTMENT, getKey());
    }


    public boolean matches(String asString) {
        if (getKey().toString().equalsIgnoreCase(asString)) {
            return true;
        } else if (stringKey.equalsIgnoreCase(asString)) {
            return true;
        } else if (getKey().value().equalsIgnoreCase(asString)) {
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

    // TODO: Add all new fields from 1.20.5
    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection section = baseFile.createSection(getKey().getKey());

        section.set("enabled", isEnabled);
        section.set("aliases", getAliases());
        section.set("display_name", getDisplayName());

        section.set("supported_items", getSupportedItems().key().toString());
        section.set("primary_items", getPrimaryItems().key().toString());

        section.set("minimum_cost.base_cost", getMinimumCost().baseCost());
        section.set("minimum_cost.additional_per_level_cost", getMinimumCost().additionalPerLevelCost());
        section.set("maximum_cost.base_cost", getMaximumCost().baseCost());
        section.set("maximum_cost.additional_per_level_cost", getMaximumCost().additionalPerLevelCost());

        section.set("exclusive_with", getExclusiveSet().key().toString());
        section.set("anvil_cost", getAnvilCost());
        section.set("max_level", getMaxLevel());
        section.set("weight", getWeight());

        generationContexts.forEach(context -> context.createSection(section, "generation"));

        return section;
    }

    // TODO: Add all new fields from 1.20.5
    public void configure(ConfigurationSection section, String directory) {
        isEnabled = section.getBoolean("enabled", isEnabled);
        aliases = section.getStringList("aliases");
        displayName = section.getString("display_name", getDisplayName());

        NamespacedKey namespacedKey = parseKey(section, "supported_items", directory);
        if (namespacedKey != null) {
            supportedItems = TagKey.create(RegistryKey.ITEM, namespacedKey);
        }

        namespacedKey = parseKey(section, "primary_items", directory);
        if (namespacedKey != null) {
            primaryItems = TagKey.create(RegistryKey.ITEM, namespacedKey);
        }

        int minBaseCost = section.getInt("minimum_cost.base_cost", getMinimumCost().baseCost());
        int minExtraCost = section.getInt("minimum_cost.additional_per_level_cost", getMinimumCost().additionalPerLevelCost());

        minimumCost = EnchantmentRegistryEntry.EnchantmentCost.of(minBaseCost, minExtraCost);

        int maxBaseCost = section.getInt("maximum_cost.base_cost", getMaximumCost().baseCost());
        int maxExtraCost = section.getInt("maximum_cost.additional_per_level_cost", getMaximumCost().additionalPerLevelCost());

        maximumCost = EnchantmentRegistryEntry.EnchantmentCost.of(maxBaseCost, maxExtraCost);

        namespacedKey = parseKey(section, "exclusive_with", directory);
        if (namespacedKey != null) {
            exclusiveWith = TagKey.create(RegistryKey.ENCHANTMENT, namespacedKey);
        }

        anvilCost = section.getInt("anvil_cost", anvilCost);
        // TODO: Add hard coded "safe max level" field on implementors to let enchants limit to safe maximums?
        maxLevel = section.getInt("max_level", maxLevel);

        weight = section.getInt("weight", weight);

        ConfigurationSection generationSection = section.getConfigurationSection("generation");
        if (generationSection != null) {
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

                        WbsEnchants.getInstance().settings.logError(ex.getMessage(), errorDir);
                    }
                } else {
                    WbsEnchants.getInstance().settings.logError("Generation type must be a section: " + key,
                            contextDir);
                }
            }
        }
    }

    private NamespacedKey parseKey(ConfigurationSection section, String key, String directory) {
        String keyString = section.getString(key);
        if (keyString != null) {
            NamespacedKey namespacedKey = NamespacedKey.fromString(keyString);
            if (namespacedKey != null) {
                return namespacedKey;
            } else {
                WbsEnchants.getInstance().settings.logError("Invalid namespaced key: " + keyString,
                        directory + "/" + key);
            }
        }

        return null;
    }

    /**
     * @return Whether this enchantment is under development, and should override user configuration.
     */
    public boolean developerMode() {
        return WbsEnchants.getInstance().settings.isDeveloperMode();
    }

    public void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, WbsEnchants.getInstance());

        // These (and similar) can theoretically be called from the implementer itself, but this makes it harder to
        // accidentally forget it.
        // TODO: Create a registry of auto registration that can be iterated over instead of hardcoding into this class
        if (this instanceof AutoRegistrableEnchant autoRegistrable) {
            if (autoRegistrable.autoRegister()) {
                if (autoRegistrable instanceof DamageEnchant damageEnchant) {
                    damageEnchant.registerDamageEvent();
                }
                if (this instanceof VehicleEnchant vehicleEnchant) {
                    vehicleEnchant.registerVehicleEvents();
                }
                if (this instanceof BlockEnchant blockEnchant) {
                    blockEnchant.registerBlockEvents();
                }
                if (this instanceof NonPersistentBlockEnchant npBlockEnchant) {
                    npBlockEnchant.registerNonPersistentBlockEvents();
                }
                if (this instanceof ProjectileEnchant<?> projectileEnchant) {
                    projectileEnchant.registerProjectileEvents();
                }
            }
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

    public void sendMessage(String message, CommandSender sender) {
        WbsEnchants.getInstance().sendMessage(message, sender);
    }

    public void sendActionBar(String message, Player player) {
        WbsEnchants.getInstance().sendActionBar(message, player);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Checks if the given entity has an item enchanted with this in the hand slot, returning either
     * the item containing this enchantment, or null if it did not meet those conditions.
     * @param entity The entity whose {@link org.bukkit.inventory.EntityEquipment} to check.
     * @return An item from the given slot of the given entity, enchanted with this enchantment, or null.
     */
    @Nullable
    public ItemStack getIfEnchanted(LivingEntity entity) {
        return getIfEnchanted(entity, EquipmentSlot.HAND);
    }

    /**
     * Checks if the given entity has an item enchanted with this in the hand slot, returning either
     * the item containing this enchantment, or null if it did not meet those conditions.
     * @param entity The entity whose {@link org.bukkit.inventory.EntityEquipment} to check.
     * @param slot The slot to check for the enchanted item.
     * @return An item from the given slot of the given entity, enchanted with this enchantment, or null.
     */
    @Nullable
    public ItemStack getIfEnchanted(LivingEntity entity, EquipmentSlot slot) {
        if (slot == null) {
            return null;
        }

        if (entity == null) {
            return null;
        }

        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return null;
        }

        ItemStack item = equipment.getItem(slot);

        if (isEnchantmentOn(item)) {
            return item;
        }

        return null;
    }

    private static final EquipmentSlot[] ARMOUR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    @Nullable
    public ItemStack getHighestEnchantedArmour(LivingEntity entity) {
        return getHighestEnchanted(entity, List.of(ARMOUR_SLOTS));
    }

    @Nullable
    public ItemStack getHighestEnchanted(LivingEntity entity, Collection<EquipmentSlot> slots) {
        return slots.stream()
                .map(slot -> getIfEnchanted(entity, slot))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(this::getLevel))
                .orElse(null);
    }

    public int compareTo(WbsEnchantment other) {
        int typeComparison = getType().compareTo(other.getType());
        if (typeComparison != 0) {
            return typeComparison;
        }
        return getKey().getKey().compareTo(other.getKey().getKey());
    }

    @NotNull
    public Enchantment getEnchantment() {
        Enchantment enchantment = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(this.getKey());

        if (enchantment == null) {
            throw new IllegalStateException("Server enchantment not found for enchantment \"" + this.getKey() + "\".");
        }

        return enchantment;
    }

    public boolean isEnchantmentOn(@NotNull ItemStack item) {
        return item.containsEnchantment(getEnchantment());
    }

    public int getLevel(@NotNull ItemStack item) {
        return item.getEnchantments().getOrDefault(getEnchantment(), 0);
    }

    public abstract String getDefaultDisplayName();
    public String getDisplayName() {
        if (this.displayName == null) {
            this.displayName = getDefaultDisplayName();
        }

        return this.displayName;
    }

    // Costs based on Smite by default, just feels like the most middle enchant -- less common than prot
    // or unbreaking, not as rare as thorns or infinity

    protected EnchantmentRegistryEntry.EnchantmentCost getDefaultMaximumCost() {
        return EnchantmentRegistryEntry.EnchantmentCost.of(25, 8);
    }

    protected EnchantmentRegistryEntry.EnchantmentCost getDefaultMinimumCost() {
        return EnchantmentRegistryEntry.EnchantmentCost.of(5, 8);
    }

    @Nullable
    public TagKey<ItemType> getDefaultPrimaryItems() {
        return WbsEnchantsBootstrap.ITEM_EMPTY;
    }
    public TagKey<Enchantment> getDefaultExclusiveSet() {
        return WbsEnchantsBootstrap.ENCHANTMENT_EMPTY;
    }

    @NotNull
    protected EquipmentSlotGroup getActiveSlots() {
        return EquipmentSlotGroup.ANY;
    }

    @NotNull
    protected EnchantmentRegistryEntry.EnchantmentCost getMaximumCost() {
        return this.maximumCost;
    }

    @NotNull
    protected EnchantmentRegistryEntry.EnchantmentCost getMinimumCost() {
        return this.minimumCost;
    }

    public final int getWeight() {
        return this.weight;
    }
    public final int getMaxLevel() {
        return this.maxLevel;
    }
    public final int getAnvilCost() {
        return this.anvilCost;
    }

    @NotNull
    public final TagKey<ItemType> getSupportedItems() {
        // Why doesn't java have a null coalescing operator </3
        if (this.supportedItems == null) {
            this.supportedItems = WbsEnchantsBootstrap.ITEM_EMPTY;
            // Using standard out, as this method needs to be called in bootstrap
            System.out.println("supported items was null, defaulting to: " + this.supportedItems);
        }

        return this.supportedItems;
    }
    @NotNull
    public final TagKey<ItemType> getPrimaryItems() {
        return Objects.requireNonNullElseGet(this.primaryItems, this::getDefaultPrimaryItems);
    }
    @NotNull
    public final TagKey<Enchantment> getExclusiveSet() {
        return Objects.requireNonNullElseGet(this.exclusiveWith, this::getDefaultExclusiveSet);
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
    public List<TagKey<Enchantment>> addToTags() {
        return List.of();
    }

    public void buildTo(RegistryFreezeEvent<Enchantment, EnchantmentRegistryEntry.@NotNull Builder> event,
                        EnchantmentRegistryEntry.Builder builder) {
        builder.description(displayName())
                .supportedItems(event.getOrCreateTag(getSupportedItems()))
                .primaryItems(event.getOrCreateTag(getPrimaryItems()))
                .minimumCost(getMinimumCost())
                .maximumCost(getMaximumCost())
                .activeSlots(getActiveSlots())
                .exclusiveWith(event.getOrCreateTag(getExclusiveSet()))
                .anvilCost(getAnvilCost())
                .maxLevel(getMaxLevel())
                .weight(getWeight());
    }

    public Component getHoverText() {
        return getHoverText(null);
    }
    public Component getHoverText(@Nullable EnumSet<HoverOptions> options) {
        if (options == null) {
            options = EnumSet.allOf(HoverOptions.class);
        }

        WbsMessageBuilder builder = WbsEnchants.getInstance().buildMessage("&h&m        &h ")
                .append(displayName())
                .append(" &h&m        &h");

        if (options.contains(HoverOptions.MAX_LEVEL)) {
            builder.append("\n&rMax level: &h" + RomanNumerals.toRoman(getMaxLevel()) + " (" + getMaxLevel() + ")");
        }
        if (options.contains(HoverOptions.TARGET)) {
            builder.append("\n&rTarget: &h" + getTargetDescription());
        }
        if (options.contains(HoverOptions.DESCRIPTION)) {
            builder.append("\n&rDescription: &h" + getDescription());
        }

        return builder.toComponent();
    }

    public Component displayName() {
        return Component.text(getDisplayName()).color(getType().getColour());
    }

    public enum HoverOptions {
        MAX_LEVEL,
        TARGET,
        DESCRIPTION
    }
}
