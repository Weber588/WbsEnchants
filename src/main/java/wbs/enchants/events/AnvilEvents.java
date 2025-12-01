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
                int firstItemCost = firstItem.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0);
                int secondItemCost = secondItem.getDataOrDefault(DataComponentTypes.REPAIR_COST, 0);

                int newCost = Math.max(firstItemCost, secondItemCost);
                newResult.setData(DataComponentTypes.REPAIR_COST, newCost * 2 + 1);
                createdResult = true;
            }

            mergeEnchantsOnto(newResult, firstItem);
            mergeEnchantsOnto(newResult, secondItem);

            combineEnchants(firstItem, secondItem, newResult);

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

    private static void combineEnchants(ItemStack firstItem, ItemStack secondItem, ItemStack newResult) {
        Map<Enchantment, Integer> firstEnchants = EnchantUtils.getEnchants(firstItem);
        Map<Enchantment, Integer> secondEnchants = EnchantUtils.getEnchants(secondItem);
        Map<Enchantment, Integer> resultEnchants = EnchantUtils.getEnchants(newResult);

        for (Map.Entry<Enchantment, Integer> firstEnchant : firstEnchants.entrySet()) {
            for (Map.Entry<Enchantment, Integer> secondEnchant : secondEnchants.entrySet()) {
                if (resultEnchants.containsKey(firstEnchant.getKey())) {
                    if (secondEnchant.getKey().equals(firstEnchant.getKey())) {
                        if (firstEnchant.getValue().equals(secondEnchant.getValue())) {
                            if (firstEnchant.getKey().getMaxLevel() > firstEnchant.getValue()) {
                                resultEnchants.put(firstEnchant.getKey(), firstEnchant.getValue() + 1);
                            }
                        }
                    }
                }
            }
        }

        EnchantUtils.addEnchantments(newResult, resultEnchants);
    }

    private static void mergeEnchantsOnto(ItemStack to, ItemStack from) {
        Map<Enchantment, Integer> resultEnchantments = EnchantUtils.getEnchants(to);

        Map<Enchantment, Integer> enchantments = EnchantUtils.getEnchants(from);

        for (Enchantment originalEnchant : enchantments.keySet()) {
            int originalLevel = enchantments.get(originalEnchant);
            if (resultEnchantments.containsKey(originalEnchant)) {
                int resultLevel = to.getEnchantmentLevel(originalEnchant);
                // Accept the highest -- even if it's over max level. No need to cap them if an original had it.
                // TODO: Make this toggleable in config?
                int finalLevel = Math.max(originalLevel, resultLevel);
                EnchantUtils.addEnchantment(originalEnchant, to, finalLevel);
            } else if (EnchantUtils.canEnchant(originalEnchant, to)) {
                EnchantUtils.addEnchantment(originalEnchant, to, originalLevel);
            }
        }
    }
}
