package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockSpreadEvent;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.BlockStateEnchant;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StasisFieldEnchant extends WbsEnchantment implements BlockStateEnchant<Beacon> {
    private static final String DESCRIPTION = "A beacon enchantment that prevents random ticks in the beacon's range, " +
            "preventing things like crops/grass growing, fire spreading or going out, or saplings growing (among " +
            "other things).";

    public StasisFieldEnchant() {
        super("stasis_field", DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.BEACON)
                .maxLevel(1);
    }

    @Override
    public Class<Beacon> getStateClass() {
        return Beacon.class;
    }

    private boolean isInRange(Beacon beacon, Location location) {
        return location.distance(beacon.getLocation()) <= beacon.getEffectRange();
    }

    private List<Beacon> getBeaconsInRadius(Block from) {
        List<Beacon> beacons = new LinkedList<>();

        int simulationDistance = Bukkit.getServer().getViewDistance() * 16;

        Collection<Chunk> chunksInRadius = from.getWorld().getIntersectingChunks(from.getBoundingBox().expand(simulationDistance));

        for (Chunk chunk : chunksInRadius) {
            chunk.getTileEntities(
                        Beacon.class::isInstance,
                        true // Use snapshots, don't actually load the chunk or state
                    )
                    .stream()
                    .map(Beacon.class::cast)
                    .filter(beacon -> isInRange(beacon, from.getLocation()))
                    .forEach(beacons::add);
        }

        return beacons;
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {

    }
}
