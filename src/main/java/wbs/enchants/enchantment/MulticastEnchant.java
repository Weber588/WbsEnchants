package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.craftbukkit.entity.CraftFishHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.FishingEnchant;
import wbs.utils.util.WbsMath;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MulticastEnchant extends WbsEnchantment implements FishingEnchant {
    private static final String DEFAULT_DESCRIPTION = "Casts an additional hook per level.";

    public MulticastEnchant() {
        super("multicast", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
                .supportedItems(ItemTypeKeys.FISHING_ROD);
    }

    @Override
    public void onHookHit(ProjectileHitEvent event, @NotNull Entity hit, ItemStack rod, EquipmentSlot hand) {
        if (event.getEntity() instanceof FishHook hook) {
            if (hook.getShooter() instanceof Player player) {
                setHook(player, hook);
            }
        }
    }

    @Override
    public void onFishEvent(PlayerFishEvent event, ItemStack rod, EquipmentSlot hand) {
        final Player player = event.getPlayer();

        final FishHook eventHook = event.getHook();

        switch (event.getState()) {
            case FISHING -> onCast(event, rod);
            case REEL_IN, CAUGHT_FISH, CAUGHT_ENTITY -> {
                if (event.getCaught() == null) {
                    reelInAll(player, rod, hand);
                } else {
                    reelInHooked(player, rod, hand, eventHook);
                    // Caught an item -- reassign hook to another random hook they own, if any exist
                    // Needs to be 1 tick later because after this event fires, the backend clears the fishing hook
                    // even if it's set to something else.
                    WbsEnchants.getInstance().runLater(() -> assignNewHook(player, eventHook), 1);
                }
            }
            case BITE -> setHook(player, eventHook);
        }
    }

    private void reelInHooked(Player player, ItemStack rod, EquipmentSlot hand, @Nullable FishHook excluding) {
        getPlayersHooks(player, excluding).stream()
                .filter(this::isHooked)
                .forEach(hook -> reelIn(player, hook, rod, hand, true));
    }

    private void assignNewHook(@NotNull Player player) {
        assignNewHook(player, null);
    }
    private void assignNewHook(@NotNull Player player, @Nullable FishHook excluding) {
        Collection<FishHook> hooks = getPlayersHooks(player, excluding);

        FishHook newHook = null;
        for (FishHook hook : hooks) {
            newHook = hook;
            if (isHooked(hook)) {
                // Use this hook -- it's going to do something.
                break;
            }
        }

        FishHook finalNewHook = newHook;
        // Needs to be 1 tick later because after this event fires, the backend clears the fishing hook
        // even if it's set to something else.
        setHook(player, finalNewHook);
    }

    private boolean isHooked(FishHook hook) {
        return hook.getHookedEntity() != null || getNibbleTicksRemaining(hook) > 0;
    }

    private Collection<FishHook> getPlayersHooks(Player player) {
        return getPlayersHooks(player, null);
    }
    private Collection<FishHook> getPlayersHooks(Player player, @Nullable FishHook excluding) {
        Collection<FishHook> allHooks = player.getWorld().getEntitiesByClass(FishHook.class);

        Set<FishHook> set = new HashSet<>();
        for (FishHook hook : allHooks) {
            if (hook.equals(excluding)) {
                continue;
            }

            net.minecraft.world.entity.Entity owner = ((CraftFishHook) hook).getHandle().getOwner();

            if (owner != null && owner.getUUID().equals(player.getUniqueId())) {
                set.add(hook);
            }
        }
        return set;
    }

    private void reelInAll(Player player, ItemStack rod, EquipmentSlot hand) {
        Collection<FishHook> hooks = player.getWorld().getEntitiesByClass(FishHook.class);

        for (FishHook hook : hooks) {
            net.minecraft.world.entity.Entity owner = ((CraftFishHook) hook).getHandle().getOwner();
            if (owner != null && owner.getUUID().equals(player.getUniqueId())) {
                reelIn(player, hook, rod, hand, true);
            }
        }
    }

    private void onCast(PlayerFishEvent event, ItemStack rod) {
        // If any hooks are already cast by this player, cancel and set to first found.
        Collection<FishHook> hooks = event.getPlayer().getWorld().getEntitiesByClass(FishHook.class);
        for (FishHook hook : hooks) {
            ProjectileSource shooter = hook.getShooter();
            if (shooter != null && shooter.equals(event.getPlayer())) {
                event.setCancelled(true);

                // The player already has a bobber somewhere -- don't create more.
                return;
            }
        }

        if (isEnchantmentOn(rod)) {
            int level = getLevel(rod);

            FishHook originalHook = event.getHook();
            for (int i = 0; i < level; i++) {
                Vector velocity = originalHook.getVelocity();
                double length = velocity.length();
                velocity = velocity.add(WbsMath.randomVector(length / 5)).normalize().multiply(length);

                FishHook newHook = event.getPlayer().launchProjectile(FishHook.class, velocity);

                setLuck(newHook, getLuck(originalHook));
                setLureSpeed(newHook, getLureSpeed(originalHook));

                newHook.setApplyLure(originalHook.getApplyLure());
                newHook.setMaxLureTime(originalHook.getMaxLureTime());
                newHook.setMinLureTime(originalHook.getMinLureTime());
                newHook.setMaxWaitTime(originalHook.getMaxWaitTime());
                newHook.setMinWaitTime(originalHook.getMinWaitTime());

                // Allow to work with hellhook
                newHook.setVisualFire(originalHook.getVisualFire());
            }
        }
    }
}
