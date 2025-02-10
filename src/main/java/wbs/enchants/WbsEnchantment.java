package wbs.enchants;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.definition.EnchantmentExtension;
import wbs.enchants.enchantment.helper.*;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.CooldownManager;
import wbs.utils.util.string.WbsStrings;

import java.util.*;

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
        this(new EnchantmentDefinition(WbsEnchantsBootstrap.createKey(key), Component.text(displayName))
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
        // Times I've forgotten so far: 3
        // TODO: Create a registry of auto registration that can be iterated over instead of hardcoding into this class
        if (this instanceof AutoRegistrableEnchant autoRegistrable) {
            if (autoRegistrable.autoRegister()) {
                if (autoRegistrable instanceof DamageEnchant damageEnchant) {
                    damageEnchant.registerDamageEvent();
                }
                if (this instanceof EntityEnchant entityEnchant) {
                    entityEnchant.registerEntityEnchants();

                    if (this instanceof VehicleEnchant vehicleEnchant) {
                        vehicleEnchant.registerVehicleEvents();
                    }
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
                if (this instanceof SpongeEnchant spongeEnchant) {
                    spongeEnchant.registrySpongeEvents();
                }
                if (this instanceof ItemModificationEnchant itemModEnchant) {
                    itemModEnchant.registerModificationEvents();
                }
                if (this instanceof FishingEnchant fishingEnchant) {
                    fishingEnchant.registerFishingEvents();
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

    protected int getSumLevels(LivingEntity entity, Collection<EquipmentSlot> slots) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return 0;
        }

        int sum = 0;
        for (EquipmentSlot slot : slots) {
            if (!entity.canUseEquipmentSlot(slot)) {
                continue;
            }

            ItemStack item = equipment.getItem(slot);

            sum += getLevel(item);
        }

        return sum;
    }

    protected int getSumLevelsArmour(LivingEntity entity) {
        return getSumLevels(entity, List.of(ARMOUR_SLOTS));
    }

    private int getSumLevels(LivingEntity entity, Set<EquipmentSlotGroup> slotGroups) {
        Set<EquipmentSlot> slots = new HashSet<>();
        Arrays.stream(EquipmentSlot.values()).forEach(slot -> {
            if (slotGroups.stream().anyMatch(group -> group.test(slot))) {
                slots.add(slot);
            }
        });

        return getSumLevels(entity, slots);
    }

    protected int getSumLevels(LivingEntity entity) {
        return getSumLevels(entity, getDefinition().getEnchantment().getActiveSlotGroups());
    }

    public int compareTo(WbsEnchantment other) {
        return getDefinition().compareTo(other.getDefinition());
    }


    public int getLevel(@NotNull ItemStack item) {
        return item.getEnchantments().getOrDefault(getEnchantment(), 0);
    }

    public @NotNull EnchantmentDefinition getDefinition() {
        return definition;
    }

    /**
     * Starts a new cooldown with this enchantment's key, if off cooldown according to given ticks.
     * @param holder The holder of the cooldown
     * @param cooldownTicks How many ticks must have passed since the cooldown started, to start a new one.
     * @return True if a cooldown was started, false if it hasn't been long enough.
     */
    protected boolean newCooldown(PersistentDataHolder holder, int cooldownTicks) {
        if (CooldownManager.getTimeSinceStart(holder, getKey()) >= cooldownTicks) {
            CooldownManager.startCooldown(holder, getKey());
            return true;
        }

        return false;
    }
}
