package wbs.enchants.enchantment;

import io.papermc.paper.event.player.PlayerFailMoveEvent;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import net.kyori.adventure.util.Ticks;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.util.CooldownManager;

import java.util.Optional;

public class LaunchingEnchant extends WbsEnchantment {
    private static final NamespacedKey IS_LAUNCHING = WbsEnchantsBootstrap.createKey("is_launching");
    private static final NamespacedKey COOLDOWN = WbsEnchantsBootstrap.createKey("launch_cooldown");

    private static final @NotNull String DEFAULT_DESCRIPTION = "Gives you an upwards boost when you start gliding while sneaking.";

    public double speedPerLevel = 0.6;
    public double cooldownSeconds = 5;
    public double maxBlocksAboveGround = 2;
    public int moveCheckBypassTicks = 5;

    public LaunchingEnchant() {
        super("launching", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.ELYTRA)
                .maxLevel(3)
                .weight(1)
                .anvilCost(4);
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        super.configure(section, directory);

        speedPerLevel = section.getDouble("speed-per-level", speedPerLevel);
        cooldownSeconds = section.getDouble("cooldown-seconds", cooldownSeconds);
        maxBlocksAboveGround = section.getDouble("max-blocks-above-ground", maxBlocksAboveGround);
        moveCheckBypassTicks = section.getInt("move-check-bypass-ticks", moveCheckBypassTicks);
    }

    @EventHandler
    public void onStartGliding(EntityToggleGlideEvent event) {
        if (!event.isGliding()) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!player.isSneaking()) {
            return;
        }

        ItemStack highestEnchanted = getHighestEnchanted(player);
        if (highestEnchanted != null) {
            Location location = player.getLocation();
            // If off cooldown, start one. If on cooldown, check if player is within 5 blocks of the ground to do it anyway.
            if (location.getWorld().rayTraceBlocks(location, new Vector(0, -1, 0), maxBlocksAboveGround) != null &&
                    CooldownManager.newCooldown(player, (int) (cooldownSeconds * Ticks.TICKS_PER_SECOND), COOLDOWN)
            ) {
                // Force start the cooldown in case this was an override by being close to the ground
                CooldownManager.startCooldown(player, COOLDOWN);

                int level = getLevel(highestEnchanted);

                // This causes desync when client shows going forward and then abruptly stops :(
                //    entity.setVelocity(entity.getVelocity().add(new Vector(0, (double) level / 1.5, 0)));

                player.getPersistentDataContainer().set(IS_LAUNCHING, PersistentDataType.BOOLEAN, true);

                // Use explosion packet to get delta movement sent to player, not just setting velocity
                ((CraftPlayer) player).getHandle().connection.send(
                        new ClientboundExplodePacket(
                                CraftLocation.toVec3(location),
                                Optional.of(new Vec3(0, level * speedPerLevel, 0)),
                                ParticleTypes.GUST_EMITTER_SMALL,
                                Holder.direct(SoundEvents.WIND_CHARGE_BURST.value())
                        )
                );

                WbsEnchants.getInstance().runLater(() -> {
                    if (player.isValid()) {
                        player.getPersistentDataContainer().remove(IS_LAUNCHING);
                    }
                }, moveCheckBypassTicks);
            }
        }
    }

    @EventHandler
    public void onFailMove(PlayerFailMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getPersistentDataContainer().has(IS_LAUNCHING)) {
            event.setAllowed(true);

            WbsEnchants.getInstance().runLater(() -> {
                if (player.isValid()) {
                    player.getPersistentDataContainer().remove(IS_LAUNCHING);
                }
            }, moveCheckBypassTicks);
        }
    }
}
