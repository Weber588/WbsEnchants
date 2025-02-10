package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.FishingEnchant;
import wbs.utils.util.WbsMath;

import java.util.Collection;

public class MulticastEnchant extends WbsEnchantment implements FishingEnchant {
    private static final String DEFAULT_DESCRIPTION = "Casts an additional hook per level.";

    public MulticastEnchant() {
        super("multicast", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
                .supportedItems(ItemTypeKeys.FISHING_ROD);
    }

    @Override
    public void onFishEvent(PlayerFishEvent event, ItemStack rod, EquipmentSlot hand) {
        final Player player = event.getPlayer();

        switch (event.getState()) {
            case FISHING -> onCast(event, rod);
            case FAILED_ATTEMPT -> reelInAll(player, rod, hand);
            case REEL_IN, CAUGHT_FISH -> {
                if (event.getCaught() == null) {
                    reelInAll(player, rod, hand);
                } else {
                    // Caught an item -- reassign hook to another random hook they own, if any exist
                    Collection<FishHook> hooks = player.getWorld().getEntitiesByClass(FishHook.class);

                    FishHook backupHook = null;
                    for (FishHook hook : hooks) {
                        ProjectileSource shooter = hook.getShooter();
                        if (shooter != null && shooter.equals(player)) {
                            backupHook = hook;
                            if (hook.getHookedEntity() != null) {
                                setHook(player, hook);
                                return;
                            }
                        }
                    }

                    setHook(player, backupHook);
                }
            }
            case BITE, CAUGHT_ENTITY -> setHook(player, event.getHook());
        }
    }

    private void reelInAll(Player player, ItemStack rod, EquipmentSlot hand) {
        Collection<FishHook> hooks = player.getWorld().getEntitiesByClass(FishHook.class);

        for (FishHook hook : hooks) {
            ProjectileSource shooter = hook.getShooter();
            if (shooter != null && shooter.equals(player)) {
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

            for (int i = 0; i < level; i++) {
                Vector velocity = event.getHook().getVelocity();
                double length = velocity.length();
                velocity = velocity.add(WbsMath.randomVector(length / 5)).normalize().multiply(length);

                event.getPlayer().launchProjectile(FishHook.class, velocity);
            }
        }
    }
}
