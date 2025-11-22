package wbs.enchants.events;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
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

        if (firstItem == null || firstItem.isEmpty() || secondItem == null || secondItem.isEmpty()) {
            return;
        }

        // Doing a combination
        if ((EnchantUtils.hasEnchants(firstItem) || EnchantUtils.hasEnchants(secondItem))) {
            boolean createdResult = false;
            ItemStack newResult = result;
            if (result == null || result.isEmpty()) {
                newResult = firstItem.clone();
                createdResult = true;
            }

            mergeEnchantsOnto(newResult, firstItem, createdResult);
            mergeEnchantsOnto(newResult, secondItem, createdResult);

            if (createdResult && !newResult.equals(firstItem)) {
                event.setResult(newResult);
                event.getView().setRepairCost(newResult.getEnchantments().keySet().stream().mapToInt(Enchantment::getAnvilCost).sum());
            }
        }

        if (result == null || result.isEmpty()) {
            return;
        }

        if (WbsEnchants.getInstance().getSettings().disableAnvilRepairPenalty()) {
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

    private static void mergeEnchantsOnto(ItemStack to, ItemStack from, boolean combine) {
        Map<Enchantment, Integer> resultEnchantments = new HashMap<>(to.getEnchantments());
        ItemEnchantments data = to.getData(DataComponentTypes.STORED_ENCHANTMENTS);
        if (data != null) {
            resultEnchantments.putAll(data.enchantments());
        }

        Map<Enchantment, Integer> enchantments = from.getEnchantments();
        ItemEnchantments fromData = to.getData(DataComponentTypes.STORED_ENCHANTMENTS);
        if (fromData != null) {
            enchantments.putAll(fromData.enchantments());
        }

        for (Enchantment originalEnchant : enchantments.keySet()) {
            if (resultEnchantments.containsKey(originalEnchant)) {
                int originalLevel = enchantments.get(originalEnchant);
                int resultLevel = to.getEnchantmentLevel(originalEnchant);
                // Accept the highest -- even if it's over max level. No need to cap them if an original had it.
                // TODO: Make this toggleable in config?
                int finalLevel = Math.max(originalLevel, resultLevel);
                if (originalLevel == resultLevel && combine && finalLevel < originalEnchant.getMaxLevel()) {
                    finalLevel++;
                }
                EnchantUtils.addEnchantment(originalEnchant, to, finalLevel);
            }
        }
    }
}
