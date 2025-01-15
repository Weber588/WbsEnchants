package wbs.enchants.enchantment;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.enchants.enchantment.helper.SpongeEnchant;

import java.util.LinkedList;
import java.util.List;

public class PyroclasticEnchant extends WbsEnchantment implements BlockEnchant, SpongeEnchant {
    private static final int VANILLA_SPONGE_RADIUS = 7;
    private static final String DEFAULT_DESCRIPTION = "The sponge absorbs lava instead of water.";

    public PyroclasticEnchant() {
        super("pyroclastic", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.SPONGES)
                .exclusiveWith(
                        EnchantManager.HYDROPHOBIC
                ).targetDescription("Sponge");
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getType() == Material.SPONGE || block.getType() == Material.WET_SPONGE;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        Block sponge = event.getBlock();
        Integer level = getLevel(sponge);
        if (level == null) {
            return;
        }

        // Check what it currently is, not what it's going to be (.getBlock first)
        boolean absorbedWater = event.getBlocks().stream().anyMatch(state -> state.getBlock().getType() == Material.WATER);

        // This enchantment cannot absorb water -- it's only for lava.
        if (absorbedWater) {
            event.setCancelled(true);
        }
    }

    @Override
    public void afterPlace(BlockPlaceEvent event, ItemStack placedItem) {
        Block sponge = event.getBlock();
        Integer level = getLevel(sponge);
        if (level == null) {
            return;
        }

        List<BlockState> toAbsorb = new LinkedList<>();
        SpongeAbsorbEvent absorbEvent = new SpongeAbsorbEvent(sponge, toAbsorb);

        int spongeRange = VANILLA_SPONGE_RADIUS;
        for (int x = -spongeRange; x < spongeRange; x++) {
            for (int y = -spongeRange; y < spongeRange; y++) {
                for (int z = -spongeRange; z < spongeRange; z++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) <= spongeRange) {
                        Block checkBlock = sponge.getLocation().clone().add(x, y, z).getBlock();
                        if (checkBlock.getType() == Material.LAVA) {
                            BlockState state = checkBlock.getState();
                            state.setType(Material.AIR);

                            toAbsorb.add(state);
                        }
                    }
                }
            }
        }

        if (toAbsorb.isEmpty()) {
            return;
        }

        Bukkit.getPluginManager().callEvent(absorbEvent);
        if (!absorbEvent.isCancelled()) {
            for (BlockState toChange : toAbsorb) {
                // Force update to allow changing types
                toChange.update(true);
            }
        }
    }
}
