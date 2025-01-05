package wbs.enchants;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.enchantment.helper.*;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.string.WbsStrings;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public abstract class WbsEnchantment implements Comparable<WbsEnchantment>, Listener, EnchantmentExtension {
    @NotNull
    private final EnchantmentDefinition definition;

    public WbsEnchantment(@NotNull EnchantmentDefinition definition) {
        this.definition = definition;

        EnchantManager.register(this);
    }

    public WbsEnchantment(@NotNull String key, @NotNull String description) {
        this(key, EnchantmentTypeManager.REGULAR, description);
    }
    public WbsEnchantment(@NotNull String key, @NotNull String displayName, @NotNull String description) {
        this(key, EnchantmentTypeManager.REGULAR, displayName, description);
    }
    public WbsEnchantment(@NotNull String key, EnchantmentType type, @NotNull String description) {
        this(key, type, WbsStrings.capitalizeAll(key.replaceAll("_", " ")), description);
    }
    public WbsEnchantment(@NotNull String key, EnchantmentType type, @NotNull String displayName, @NotNull String description) {
        this(new EnchantmentDefinition(WbsEnchantsBootstrap.createKey(key), Component.text(displayName).color(type.getColour()))
                .description(description)
                .type(type));
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
        // Times I've forgotten so far: 2
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
                if (this instanceof ShieldBlockEnchant shieldBlockEnchant) {
                    shieldBlockEnchant.registerShieldBlockEvent();
                }
            }
        }
    }

    public void sendMessage(String message, CommandSender sender) {
        WbsEnchants.getInstance().sendMessage(message, sender);
    }

    public void sendActionBar(String message, Player player) {
        WbsEnchants.getInstance().sendActionBar(message, player);
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
        int typeComparison = type().compareTo(other.type());
        if (typeComparison != 0) {
            return typeComparison;
        }
        return key().compareTo(other.key());
    }


    public int getLevel(@NotNull ItemStack item) {
        return item.getEnchantments().getOrDefault(getEnchantment(), 0);
    }

    public @NotNull EnchantmentDefinition getDefinition() {
        return definition;
    }


}
