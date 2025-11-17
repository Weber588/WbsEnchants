package wbs.enchants.enchantment.helper;

import net.minecraft.world.InteractionHand;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftFishHook;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
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
        EventUtils.register(PlayerFishEvent.class, this::onFishEvent, EventPriority.HIGH, true);
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

        int damage = ((CraftFishHook) hook).getHandle().retrieve(
                ((CraftItemStack) item).handle,
                hand == EquipmentSlot.HAND ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND
        );

        item.damage(damage, player);

        if (suppressEvent) {
            REELING.remove(player.getUniqueId());
        }
    }

    default void setHook(Player player, FishHook hook) {
        ((CraftPlayer) player).getHandle().fishing = ((CraftFishHook) hook).getHandle();
    }
}
