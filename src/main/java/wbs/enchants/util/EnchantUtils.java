package wbs.enchants.util;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import me.sciguymjm.uberenchant.api.utils.UberConfiguration;
import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.events.EnchantsModifyEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantUtils {
    public static boolean willConflict(Enchantment a, Enchantment b) {
        if (directlyConflictsWith(a, b)) {
            return true;
        }

        return getConflictsWith(b).contains(a);
    }

    public static List<Enchantment> getConflictsWith(Enchantment enchant) {
        return getConflictsWith(enchant, getAllEnchants());
    }

    public static List<Enchantment> getConflictsWith(Enchantment enchant, List<Enchantment> toCheck) {
        return toCheck.stream()
                .filter(check -> directlyConflictsWith(enchant, check))
                .collect(Collectors.toList());
    }

    public static boolean directlyConflictsWith(Enchantment a, Enchantment b) {
        return a == b || a.equals(b) || WbsEnchantment.matches(a, b) || a.conflictsWith(b) || b.conflictsWith(a);
    }

    public static List<Enchantment> getAllEnchants() {
        return UberConfiguration.UberRecord.values()
                .stream()
                .map(UberConfiguration.UberRecord::enchantment)
                .collect(Collectors.toList());
    }

    public static void addEnchantment(UberEnchantment enchant, ItemStack item, int level) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            UberUtils.addStoredEnchantment(enchant, item, level);
        } else {
            Map<Enchantment, Integer> oldEnchants = item.getEnchantments();

            UberUtils.addEnchantment(enchant, item, level);

            HashMap<Enchantment, Integer> updatedEnchants = new HashMap<>(oldEnchants);
            updatedEnchants.put(enchant, level);

            EnchantsModifyEvent modifyEvent = new EnchantsModifyEvent(item, oldEnchants, updatedEnchants);

            Bukkit.getPluginManager().callEvent(modifyEvent);
        }
    }
}
