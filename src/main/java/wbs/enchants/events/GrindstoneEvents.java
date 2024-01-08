package wbs.enchants.events;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GrindstoneEvents implements Listener {
    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        ItemStack inputTop = event.getInventory().getItem(0);
        ItemStack inputBottom = event.getInventory().getItem(1);

        Set<UberEnchantment> inputEnchants = new HashSet<>();

        if (inputBottom != null) {
            inputEnchants.addAll(UberEnchantment.getEnchantments(inputBottom).keySet());
        }
        if (inputTop != null) {
            inputEnchants.addAll(UberEnchantment.getEnchantments(inputTop).keySet());
        }

        if (!inputEnchants.isEmpty()) {
            ItemStack result = event.getResult();
            if (result != null) {
                ItemMeta itemMeta = result.getItemMeta();
                if (itemMeta != null) {
                    inputEnchants.forEach(enchant -> {
                        List<String> lore = itemMeta.getLore();
                        if (lore != null) {
                            String toRemove = lore.stream()
                                    .filter(line -> {
                                        String strippedLine = ChatColor.stripColor(
                                                ChatColor.translateAlternateColorCodes(
                                                        '&', line));
                                        String strippedDisplay = ChatColor.stripColor(
                                                ChatColor.translateAlternateColorCodes(
                                                        '&', enchant.getDisplayName()));
                                        return strippedLine.startsWith(strippedDisplay);
                                    }).findFirst().orElse(null);

                            lore.remove(toRemove);
                            itemMeta.setLore(lore);
                        }
                    });
                    result.setItemMeta(itemMeta);
                }
            }

            event.setResult(result);
        }
    }
}
