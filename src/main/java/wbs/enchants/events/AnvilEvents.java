package wbs.enchants.events;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Repairable;
import io.papermc.paper.registry.TypedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.view.AnvilView;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.ItemUtils;

@SuppressWarnings("UnstableApiUsage")
public class AnvilEvents implements Listener {
    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        if (WbsEnchants.getInstance().getSettings().disableAnvilRepairPenalty()) {
            AnvilView view = event.getView();

            ItemStack firstItem = view.getItem(0);
            ItemStack secondItem = view.getItem(1);
            ItemStack result = event.getResult();

            if (result == null || result.isEmpty() || firstItem == null || firstItem.isEmpty() || secondItem == null || secondItem.isEmpty()) {
                return;
            }

            Repairable repairableData = firstItem.getData(DataComponentTypes.REPAIRABLE);

            if (repairableData != null) {
                TypedKey<ItemType> secondItemType = ItemUtils.getTypedKey(secondItem);
                if (repairableData.types().contains(secondItemType)) {
                    Integer firstItemPenalty = firstItem.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0);
                    Integer resultPenalty = result.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0);
                    if (resultPenalty > firstItemPenalty) {
                        result.setData(DataComponentTypes.REPAIR_COST, firstItemPenalty);
                        event.setResult(result);
                    }
                }
            }
        }
    }
}
