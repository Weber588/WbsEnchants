package wbs.enchants.events;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Repairable;
import io.papermc.paper.registry.TypedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.view.AnvilView;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.EnchantUtils;
import wbs.enchants.util.ItemUtils;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class AnvilEvents implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilView view = event.getView();

        ItemStack firstItem = view.getItem(0);
        ItemStack secondItem = view.getItem(1);
        ItemStack result = event.getResult();

        if (result == null || result.isEmpty() || firstItem == null || firstItem.isEmpty() || secondItem == null || secondItem.isEmpty()) {
            return;
        }

        TypedKey<ItemType> secondItemType = ItemUtils.getTypedKey(secondItem);

        if (WbsEnchants.getInstance().getSettings().disableAnvilRepairPenalty()) {
            Repairable repairableData = firstItem.getData(DataComponentTypes.REPAIRABLE);
            if (repairableData != null) {
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

        // Doing a combination
        if (secondItemType.equals(ItemUtils.getTypedKey(firstItem))) {
            mergeEnchantsOnto(result, firstItem);
            mergeEnchantsOnto(result, secondItem);
        }
    }

    private static void mergeEnchantsOnto(ItemStack to, ItemStack from) {
        Map<Enchantment, Integer> resultEnchantments = new HashMap<>(to.getEnchantments());

        Map<Enchantment, Integer> enchantments = from.getEnchantments();

        for (Enchantment originalEnchant : enchantments.keySet()) {
            if (resultEnchantments.containsKey(originalEnchant)) {
                int originalLevel = enchantments.get(originalEnchant);
                int resultLevel = to.getEnchantmentLevel(originalEnchant);
                // Accept the highest -- even if it's over max level. No need to cap them if an original had it.
                // TODO: Make this toggleable in config?
                EnchantUtils.addEnchantment(originalEnchant, to, Math.max(originalLevel, resultLevel));
            }
        }
    }
}
