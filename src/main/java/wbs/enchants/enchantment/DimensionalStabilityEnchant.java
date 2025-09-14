package wbs.enchants.enchantment;

import io.papermc.paper.event.player.PlayerBedFailEnterEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.block.Bed;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.BlockEnchant;

public class DimensionalStabilityEnchant extends WbsEnchantment implements BlockEnchant {
    private static final String DESCRIPTION = "A bed enchantment that allows you to sleep in any dimension!";

    public DimensionalStabilityEnchant() {
        super("dimensional_stability", DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.BEDS);
    }

    @EventHandler
    public void onEnterBed(PlayerBedEnterEvent event) {
        Block bed = event.getBed();
        Integer level = getLevel(bed);
        if (level == null) {
            return;
        }

        event.setUseBed(Event.Result.ALLOW);
    }

    @EventHandler
    public void onSleep(PlayerBedFailEnterEvent event) {
        Block bed = event.getBed();
        Integer level = getLevel(bed);
        if (level == null) {
            return;
        }

        PlayerBedFailEnterEvent.FailReason failReason = event.getFailReason();
        if (failReason == PlayerBedFailEnterEvent.FailReason.NOT_POSSIBLE_HERE) {
            event.setWillExplode(false);
        }
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getState() instanceof Bed;
    }
}
