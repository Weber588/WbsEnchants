package wbs.enchants.enchantment;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SpongeAbsorbEvent;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.enchants.enchantment.helper.SpongeEnchant;
import wbs.enchants.util.BlockQuery;
import wbs.utils.util.entities.selector.RadiusSelector;

import java.util.List;

public class HydrophobicEnchant extends WbsEnchantment implements BlockEnchant, SpongeEnchant {
    private static final int VANILLA_SPONGE_RADIUS = 7;
    private static final int RADIUS_PER_LEVEL = 2;
    private static final String DEFAULT_DESCRIPTION = "Increases the radius of drying by " + RADIUS_PER_LEVEL + " blocks per level.";


    public HydrophobicEnchant() {
        super("hydrophobic", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(WbsEnchantsBootstrap.SPONGES)
                .targetDescription("Sponge");
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getType() == Material.SPONGE || block.getType() == Material.WET_SPONGE;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        Block sponge = event.getBlock();
        Integer level = getLevel(sponge);
        if (level == null) {
            return;
        }

        List<BlockState> toAbsorb = event.getBlocks();

        int spongeRange = VANILLA_SPONGE_RADIUS + (level * RADIUS_PER_LEVEL);

        List<Block> connectedWater = new BlockQuery()
                .setMaxBlocks((spongeRange * 2) * (spongeRange * 2)) // Upper limit
                .setMaxDistance(spongeRange)
                .setDistanceMode(BlockQuery.DistanceMode.MANHATTAN)
                .setPredicate(check -> check.getType() == Material.WATER)
                .getVein(sponge);

        connectedWater.forEach(waterBlock -> {
            BlockState state = waterBlock.getState();
            state.setType(Material.AIR);

            toAbsorb.add(state);
        });

        // Next tick, ensure players can see the changes -- view update bug it looks like
        WbsEnchants.getInstance().runSync(() -> {
            List<Player> playersViewing = new RadiusSelector<>(Player.class)
                    .setRange(Bukkit.getServer().getViewDistance())
                    .select(sponge.getLocation());

            playersViewing.forEach(player -> player.sendBlockChanges(toAbsorb));
        });
    }
}
