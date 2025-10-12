package wbs.enchants.enchantment;

import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;

public class OutreachEnchant extends WbsEnchantment implements BlockStateEnchant<Beacon> {
    private static final int PERCENT_PER_LEVEL = 20;

    private static final String DESCRIPTION = "A beacon enchant that increases its radius by " + PERCENT_PER_LEVEL + "% per level.";

    public OutreachEnchant() {
        super("outreach", DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_BEACON)
                .maxLevel(3)
                .minimumCost(5, 8)
                .maximumCost(55, 8);
    }

    @Override
    public Class<Beacon> getStateClass() {
        return Beacon.class;
    }

    @Override
    public void afterPlace(BlockPlaceEvent event, ItemStack placedItem) {
        if (!(event.getBlock().getState() instanceof Beacon)) {
            return;
        }

        startBeaconTimer(event.getBlock());
    }

    @Override
    public void onLoad(ChunkLoadEvent event, Block block, int level) {
        if (!(block.getState() instanceof Beacon)) {
            return;
        }

        startBeaconTimer(block);
    }

    private void startBeaconTimer(Block block) {
        WbsEnchants.getInstance().runTimer(task -> {
            if (!block.getChunk().isLoaded()) {
                task.cancel();
                return;
            }

            if (!(block.getState() instanceof Beacon currentBeacon)) {
                task.cancel();
                return;
            }

            Integer currentLevel = getLevel(block);
            if (currentLevel == null) {
                task.cancel();
                return;
            }

            int tier = currentBeacon.getTier();
            int defaultRange = 10 * tier + 10;

            currentBeacon.setEffectRange((double) defaultRange * (PERCENT_PER_LEVEL + 100) / 100 * currentLevel);

            currentBeacon.update();
        }, 20, 20);
    }
}
