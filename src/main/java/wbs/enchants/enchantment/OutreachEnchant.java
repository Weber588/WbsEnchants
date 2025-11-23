package wbs.enchants.enchantment;

import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;
import wbs.enchants.enchantment.helper.TickableBlockEnchant;

public class OutreachEnchant extends WbsEnchantment implements BlockStateEnchant<Beacon>, TickableBlockEnchant {
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
    public int getTickFrequency() {
        return 60;
    }

    @Override
    public void onTick(Block block, int level) {
        if (block.getType() != Material.BEACON) {
            return;
        }

        if (!(block.getState() instanceof Beacon currentBeacon)) {
            return;
        }

        int tier = currentBeacon.getTier();
        int defaultRange = 10 * (tier + 1);

        double customRange = (double) defaultRange * (PERCENT_PER_LEVEL + 100) / 100 * level;
        if (currentBeacon.getEffectRange() != customRange) {
            currentBeacon.setEffectRange(customRange);
            currentBeacon.update();
        }
    }
}
