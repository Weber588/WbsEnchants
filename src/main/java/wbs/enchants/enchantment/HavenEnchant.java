package wbs.enchants.enchantment;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class HavenEnchant extends WbsEnchantment implements BlockStateEnchant<Beacon> {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Prevents hostile mob spawning in the beacon's radius";

    private static final WbsParticleEffect PARTICLE_EFFECT = new RingParticleEffect()
            .setRadius(0.9)
            .setSpeed(0.075)
            .setVariation(0.05)
            .setAmount(7)
            .build();

    public HavenEnchant() {
        super("haven", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_BEACON)
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(5, 8))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(55, 8));
    }

    @Override
    public Class<Beacon> getStateClass() {
        return Beacon.class;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMobSpawn(EntitySpawnEvent event) {
        Entity spawned = event.getEntity();
        if (spawned.getEntitySpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }
        if (!(spawned instanceof Enemy)) {
            return;
        }

        Location spawnLocation = event.getLocation();

        for (Beacon beacon : getBeaconsInRadius(spawnLocation.getBlock())) {
            if (isEnchanted(beacon)) {
                event.setCancelled(true);
                Location beaconLocation = beacon.getLocation().toCenterLocation();

                PARTICLE_EFFECT.buildAndPlay(Particle.SOUL, beaconLocation);

                break;
            }
        }
    }

    private boolean isInRange(Beacon beacon, Location location) {
        int tier = beacon.getTier();
        double range = 10 * tier + 10;

        if (beacon.getEffectRange() > 0) {
            range = beacon.getEffectRange();
        }

        return location.distance(beacon.getLocation()) <= range;
    }

    private List<Beacon> getBeaconsInRadius(Block from) {
        List<Beacon> beacons = new LinkedList<>();

        int simulationDistance = Bukkit.getServer().getViewDistance() * 16;

        Collection<Chunk> chunksInRadius = Arrays.stream(from.getWorld().getLoadedChunks())
                .filter(chunk -> {
                    Location location = chunk.getBlock(7, from.getY(), 7).getLocation();
                    double distanceSquared = location.distanceSquared(from.getLocation());
                    return distanceSquared < simulationDistance * simulationDistance;
                })
                .collect(Collectors.toSet());

        for (Chunk chunk : chunksInRadius) {
            chunk.getTileEntities(
                            block -> block.getState() instanceof Beacon,
                            true
                    )
                    .stream()
                    .map(Beacon.class::cast)
                    .filter(beacon -> isInRange(beacon, from.getLocation()))
                    .forEach(beacons::add);
        }

        return beacons;
    }
}
