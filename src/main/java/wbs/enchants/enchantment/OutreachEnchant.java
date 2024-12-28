package wbs.enchants.enchantment;

import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockEnchant;

public class OutreachEnchant extends WbsEnchantment implements BlockEnchant {
    private static final int PERCENT_PER_LEVEL = 20;

    private static final String DESCRIPTION = "A beacon enchant that increases its radius by " + PERCENT_PER_LEVEL + "% per level.";

    public OutreachEnchant() {
        super("outreach", DESCRIPTION);

        supportedItems = WbsEnchantsBootstrap.ENCHANTABLE_BEACON;
        maxLevel = 3;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Outreach";
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getState() instanceof Beacon;
    }

    @Override
    public void afterPlace(BlockPlaceEvent event, ItemStack placedItem) {
        if (!(event.getBlock().getState() instanceof Beacon beacon)) {
            return;
        }

        beacon.setEffectRange(beacon.getEffectRange() * (PERCENT_PER_LEVEL + 100) / 100 * getLevel(placedItem));
        beacon.update();
    }
}
