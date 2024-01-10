package wbs.enchants.util;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import org.bukkit.enchantments.Enchantment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantUtils {
    public static boolean willConflict(Enchantment a, Enchantment b) {
        if (directlyConflictsWith(a, b)) {
            return true;
        }

        return getConflictsWith(a).contains(b);
    }

    public static List<Enchantment> getConflictsWith(Enchantment enchant) {
        return getAllEnchants().stream()
                .filter(check -> directlyConflictsWith(enchant, check))
                .collect(Collectors.toList());
    }

    public static boolean directlyConflictsWith(Enchantment a, Enchantment b) {
        if (a == b) {
            return true;
        }

        if (a.conflictsWith(b)) {
            return true;
        } else if (b.conflictsWith(a)) {
            return true;
        }

        return false;
    }

    public static List<Enchantment> getAllEnchants() {
        List<Enchantment> allEnchants = Arrays.stream(Enchantment.values()).toList();
        allEnchants.addAll(UberEnchantment.getRegisteredEnchantments());
        return allEnchants;
    }
}
