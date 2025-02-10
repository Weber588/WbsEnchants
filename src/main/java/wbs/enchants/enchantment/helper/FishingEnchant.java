package wbs.enchants.enchantment.helper;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.util.EventUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface FishingEnchant extends EnchantInterface, AutoRegistrableEnchant {
    Set<UUID> REELING = new HashSet<>();
    Set<PlayerFishEvent.State> REELING_SUPPRESSED = Set.of(
            PlayerFishEvent.State.REEL_IN,
            PlayerFishEvent.State.CAUGHT_FISH,
            PlayerFishEvent.State.FAILED_ATTEMPT
    );

    default void registerFishingEvents() {
        EventUtils.register(PlayerFishEvent.class, this::onFishEvent, EventPriority.LOWEST, true);
        EventUtils.register(ProjectileHitEvent.class, this::onHookHit, EventPriority.MONITOR, true);
    }

    default void onFishEvent(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (REELING_SUPPRESSED.contains(event.getState()) && REELING.contains(player.getUniqueId())) {
            return;
        }

        EquipmentSlot hand = event.getHand();
        PlayerInventory inventory = player.getInventory();

        if (hand == null) {
            if (inventory.getItem(EquipmentSlot.HAND).getType() == Material.FISHING_ROD) {
                hand = EquipmentSlot.HAND;
            } else if (inventory.getItem(EquipmentSlot.OFF_HAND).getType() == Material.FISHING_ROD) {
                hand = EquipmentSlot.OFF_HAND;
            } else {
                return;
            }
        }

        ItemStack rod = inventory.getItem(hand);

        if (!getThisEnchantment().isEnchantmentOn(rod)) {
            return;
        }

        onFishEvent(event, rod, hand);
    }

    default void onHookHit(ProjectileHitEvent event) {
        Entity hit = event.getHitEntity();
        if (hit == null) {
            return;
        }

        if (!(event.getEntity() instanceof FishHook hook)) {
            return;
        }
        if (!(hook.getShooter() instanceof Player player)) {
            return;
        }

        PlayerInventory inventory = player.getInventory();

        EquipmentSlot hand;
        if (inventory.getItem(EquipmentSlot.HAND).getType() == Material.FISHING_ROD) {
            hand = EquipmentSlot.HAND;
        } else if (inventory.getItem(EquipmentSlot.OFF_HAND).getType() == Material.FISHING_ROD) {
            hand = EquipmentSlot.OFF_HAND;
        } else {
            return;
        }

        ItemStack rod = inventory.getItem(hand);

        if (!getThisEnchantment().isEnchantmentOn(rod)) {
            return;
        }

        onHookHit(event, hit, rod, hand);
    }

    void onFishEvent(PlayerFishEvent event, ItemStack rod, EquipmentSlot hand);
    default void onHookHit(ProjectileHitEvent event, @NotNull Entity hit, ItemStack rod, EquipmentSlot hand) {}

    default void reelIn(Player player, FishHook hook, ItemStack item, EquipmentSlot hand) {
        reelIn(player, hook, item, hand, false);
    }
    default void reelIn(Player player, FishHook hook, ItemStack item, EquipmentSlot hand, boolean suppressEvent) {
        if (suppressEvent) {
            REELING.add(player.getUniqueId());
        }
        // Awful terrible very bad reflection until this gets added to the API
        try {
            Object handle = reflectiveGet(hook, "getHandle");

            if (handle != null) {
                Object nmsItem = item.getClass().getMethod("unwrap", ItemStack.class).invoke(null, item);
                Object interactionHand = reflectiveGet(Class.forName("org.bukkit.craftbukkit.CraftEquipmentSlot"), "getHand", hand);

                reflectiveGet(handle, "retrieve", interactionHand, nmsItem);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to invoke retrieve via reflection.", e);
        } finally {
            if (suppressEvent) {
                REELING.remove(player.getUniqueId());
            }
        }
    }

    default void setHook(Player player, FishHook hook) {
        REELING.add(player.getUniqueId());
        // Awful terrible very bad reflection until this gets added to the API
        try {
            Object handle = Class.forName("org.bukkit.craftbukkit.entity.CraftHumanEntity").getMethod("getHandle").invoke(player);

            if (handle != null) {
                Field fishingField = handle.getClass().getField("fishing");

                fishingField.set(handle, reflectiveGet(hook, "getHandle"));
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException |
                 NoSuchFieldException e) {
            throw new RuntimeException("Failed to set hook via reflection.", e);
        } finally {
            REELING.remove(player.getUniqueId());
        }
    }

    private static Object reflectiveGet(Class<?> clazz, String name, Object... params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getHandMethod = clazz.getMethod(name, Arrays.stream(params).map(Object::getClass).toArray(Class[]::new));
        return getHandMethod.invoke(null, params); // Static method
    }

    private static Object reflectiveGet(Object obj, String name, Object... params) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getHandMethod = obj.getClass().getMethod(name, Arrays.stream(params).map(Object::getClass).toArray(Class[]::new));
        return getHandMethod.invoke(obj, params); // Static method
    }
}
