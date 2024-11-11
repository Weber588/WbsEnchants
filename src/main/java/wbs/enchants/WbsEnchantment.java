package wbs.enchants;

import com.google.gson.Gson;
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
import wbs.enchants.util.EnchantUtils;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.string.WbsStrings;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public abstract class WbsEnchantment implements Comparable<WbsEnchantment>, Keyed, Listener {
    private final String stringKey;

    protected boolean isEnabled = true;
    @NotNull
    protected Map<Integer, Double> costForLevel = new HashMap<>();
    @NotNull
    protected List<String> aliases = new LinkedList<>();

    // TODO: Read these from config, with defaults based on what implementors provide in constructor
    protected int maxLevel = 1;
    protected int weight = 1;
    protected TagKey<ItemType> primaryItems;
    protected TagKey<ItemType> supportedItems;
    protected TagKey<Enchantment> exclusiveWith;
    protected int anvilCost;
    @NotNull
    protected String description;
    protected String targetDescription;

    protected final List<GenerationContext> generationContexts = new LinkedList<>();

    public WbsEnchantment(String key, @NotNull String description) {
        stringKey = key;
        this.description = description;
        EnchantManager.register(this);
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
            targetDescription = targetDescription.substring(0, targetDescription.lastIndexOf('/') + 1)
                    .replaceAll("_", " ");

            targetDescription = WbsStrings.capitalizeAll(targetDescription);
        }
        return targetDescription;
    }

    public String getPermission() {
        return "wbsenchants.enchantment." + getKey().getNamespace() + "." + getKey().getKey();
    }

    public int getStartLevel() {
        return 0;
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

    @NotNull
    public Map<Integer, Double> getCostForLevel() {
        return new HashMap<>();
    }

    // TODO: Add all new fields from 1.20.5
    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection section = baseFile.createSection(getKey().getKey());

        section.set("enabled", isEnabled);
        section.set("min_level", getStartLevel());
        section.set("max_level", getMaxLevel());
        section.createSection("cost_for_level", getCostForLevel());
        section.set("aliases", getAliases());

        generationContexts.forEach(context -> context.createSection(section, "generation"));

        return section;
    }

    // TODO: Add all new fields from 1.20.5
    public void configure(ConfigurationSection section, String directory) {
        isEnabled = section.getBoolean("enabled", isEnabled);
        aliases = section.getStringList("aliases");

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

        ConfigurationSection costLevelSection = section.getConfigurationSection("cost_for_level");

        if (costLevelSection != null) {
            for (String key : costLevelSection.getKeys(false)) {
                if (!key.matches("[0-9]")) {
                    WbsEnchants.getInstance().settings.logError("Invalid level: \"" + key + "\"",
                            directory + "/cost_for_level");
                    continue;
                }

                int level;
                try {
                    level = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    // Should be impossible unless they put a number larger than int limit
                    WbsEnchants.getInstance().settings.logError("An unexpected error occurred while parsing level: \""
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
        if (this instanceof DamageEnchant damageEnchant) {
            if (damageEnchant.autoRegister()) {
                damageEnchant.registerDamageEvent();
            }
        }
        if (this instanceof VehicleEnchant vehicleEnchant) {
            if (vehicleEnchant.autoRegister()) {
                vehicleEnchant.registerVehicleEvents();
            }
        }
        if (this instanceof BlockEnchant blockEnchant) {
            if (blockEnchant.autoRegister()) {
                blockEnchant.registerBlockEvents();
            }
        }
        if (this instanceof NonPersistentBlockEnchant npBlockEnchant) {
            if (npBlockEnchant.autoRegister()) {
                npBlockEnchant.registerNonPersistentBlockEvents();
            }
        }
        if (this instanceof ProjectileEnchant projectileEnchant) {
            if (projectileEnchant.autoRegister()) {
                projectileEnchant.registerProjectileEvents();
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
        return Arrays.stream(ARMOUR_SLOTS)
                .map(slot -> getIfEnchanted(entity, slot))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(this::getLevel))
                .orElse(null);
    }

    public int compareTo(WbsEnchantment other) {
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

    // Costs based on Smite by default, just feels like the most middle enchant -- less common than prot
    // or unbreaking, not as rare as thorns or infinity

    protected EnchantmentRegistryEntry.EnchantmentCost getDefaultMaximumCost() {
        return EnchantmentRegistryEntry.EnchantmentCost.of(25, 8);
    }

    protected EnchantmentRegistryEntry.EnchantmentCost getDefaultMinimumCost() {
        return EnchantmentRegistryEntry.EnchantmentCost.of(5, 8);
    }

    public int getDefaultWeight() {
        return 1;
    }
    public int getDefaultMaxLevel() {
        return 1;
    }
    public int getDefaultAnvilCost() {
        return 1;
    }

    public TagKey<ItemType> getDefaultSupportedItems() {
        return WbsEnchantsBootstrap.ITEM_EMPTY;
    }
    @Nullable
    public TagKey<ItemType> getDefaultPrimaryItems() {
        return WbsEnchantsBootstrap.ITEM_EMPTY;
    }
    public TagKey<Enchantment> getDefaultExclusiveSet() {
        return WbsEnchantsBootstrap.ENCHANTMENT_EMPTY;
    }

    // TODO: Move these to read from getEnchantment once they're implemented in paper API
    public String getDisplayName() {
        return this.getDefaultDisplayName();
    }

    @NotNull
    protected EquipmentSlotGroup getActiveSlots() {
        return EquipmentSlotGroup.ANY;
    }

    @NotNull
    protected EnchantmentRegistryEntry.EnchantmentCost getMaximumCost() {
        return EnchantmentRegistryEntry.EnchantmentCost.of(25, 8);
    }

    @NotNull
    protected EnchantmentRegistryEntry.EnchantmentCost getMinimumCost() {
        return EnchantmentRegistryEntry.EnchantmentCost.of(5, 8);
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
        builder.description(Component.text(getDefaultDisplayName())) // TODO: Update this to actual
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

    public String buildJsonDefinition() {
        JsonDefinition def = new JsonDefinition();

        def.description = getDefaultDisplayName();
        def.exclusive_set = getExclusiveSet().toString();
        def.supported_items = getSupportedItems().toString();
        def.weight = getWeight();
        def.max_level = getMaxLevel();
        def.min_cost = new JsonEnchCost(getMinimumCost());
        def.max_cost = new JsonEnchCost(getMaximumCost());
        def.anvil_cost = getAnvilCost();
        def.slots = getActiveSlots().toString();

        return def.toString();
    }

    private static class JsonDefinition {
        private String description;
        private String exclusive_set;
        private String supported_items;
        private int weight;
        private int max_level;
        private JsonEnchCost min_cost;
        private JsonEnchCost max_cost;
        private int anvil_cost;
        private String slots;

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }

    private static class JsonEnchCost {
        private int base;
        private int per_level_above_first;

        public JsonEnchCost(EnchantmentRegistryEntry.EnchantmentCost cost) {
            if (cost != null) {
                this.base = cost.baseCost();
                this.per_level_above_first = cost.additionalPerLevelCost();
            }
        }
    }
}
