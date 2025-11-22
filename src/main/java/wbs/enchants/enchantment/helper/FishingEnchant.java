package wbs.enchants.enchantment.helper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;
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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.EventUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface FishingEnchant extends EnchantInterface, AutoRegistrableEnchant {
    Set<UUID> REELING = new HashSet<>();
    Set<PlayerFishEvent.State> REELING_SUPPRESSED = Set.of(
            PlayerFishEvent.State.REEL_IN,
            PlayerFishEvent.State.CAUGHT_FISH,
            PlayerFishEvent.State.CAUGHT_ENTITY,
            PlayerFishEvent.State.FAILED_ATTEMPT
    );

    default void registerFishingEvents() {
        EventUtils.register(PlayerFishEvent.class, this::onFishEvent, EventPriority.HIGH, true);
        EventUtils.register(ProjectileHitEvent.class, this::onHookHit, EventPriority.MONITOR, true);
    }

    private void onFishEvent(PlayerFishEvent event) {
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

    private void onHookHit(ProjectileHitEvent event) {
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

    default void setHook(Player player, @Nullable FishHook hook) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        FishingHook nmsHook = hook == null ? null : ((CraftFishHook) hook).getHandle();
        serverPlayer.fishing = nmsHook;

        if (nmsHook != null) {
            nmsHook.setOwner(serverPlayer);
        }
    }

    /**
     * Gets the remaining ticks of being "bitten" -- if this value is >0, the player can currently pull in the fish.
     * @param hook The hook entity to check nibble ticks on
     * @return The remaining time until the bite is considered "missed"
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    default int getNibbleTicksRemaining(@NotNull FishHook hook) {
        FishingHook nmsHook = ((CraftFishHook) hook).getHandle();

        try {
            Field nibbleField = nmsHook.getClass().getDeclaredField("nibble");

            nibbleField.setAccessible(true);

            return (Integer) nibbleField.get(nmsHook);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            WbsEnchants.getInstance().getLogger().warning("Failed to get nibble field from fishing hook.");
            return 0;
        }
    }

    default void setNibbleTicksRemaining(@NotNull FishHook hook, int nibbleTicks) {
        FishingHook nmsHook = ((CraftFishHook) hook).getHandle();

        try {
            Field nibbleField = nmsHook.getClass().getDeclaredField("nibble");

            nibbleField.setAccessible(true);

            nibbleField.set(nmsHook, nibbleTicks);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            WbsEnchants.getInstance().getLogger().warning("Failed to get nibble field from fishing hook.");
            //throw new RuntimeException(e);
        }
    }

    default int getLuck(@NotNull FishHook hook) {
        FishingHook nmsHook = ((CraftFishHook) hook).getHandle();

        try {
            Field luckField = nmsHook.getClass().getDeclaredField("luck");

            luckField.setAccessible(true);

            return (Integer) luckField.get(nmsHook);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            WbsEnchants.getInstance().getLogger().warning("Failed to get luck field from fishing hook.");
            // throw new RuntimeException(e);
            return 0;
        }
    }

    default void setLuck(@NotNull FishHook hook, int luck) {
        FishingHook nmsHook = ((CraftFishHook) hook).getHandle();

        try {
            Field luckField = nmsHook.getClass().getDeclaredField("luck");

            luckField.setAccessible(true);

            luckField.set(nmsHook, luck);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            WbsEnchants.getInstance().getLogger().warning("Failed to get luck field from fishing hook.");
            // throw new RuntimeException(e);
        }
    }

    default int getLureSpeed(@NotNull FishHook hook) {
        FishingHook nmsHook = ((CraftFishHook) hook).getHandle();

        try {
            Field lureSpeedField = nmsHook.getClass().getDeclaredField("lureSpeed");

            lureSpeedField.setAccessible(true);

            return (Integer) lureSpeedField.get(nmsHook);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            WbsEnchants.getInstance().getLogger().warning("Failed to get lureSpeed field from fishing hook.");
            return 0;
           // throw new RuntimeException(e);
        }
    }

    default void setLureSpeed(@NotNull FishHook hook, int lureSpeed) {
        FishingHook nmsHook = ((CraftFishHook) hook).getHandle();

        try {
            Field lureSpeedField = nmsHook.getClass().getDeclaredField("lureSpeed");

            lureSpeedField.setAccessible(true);

            lureSpeedField.set(nmsHook, lureSpeed);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            WbsEnchants.getInstance().getLogger().warning("Failed to get lureSpeed field from fishing hook.");
            //throw new RuntimeException(e);
        }
    }
}
