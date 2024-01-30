package wbs.enchants.util;

import me.sciguymjm.uberenchant.api.utils.UberConfiguration;
import org.bukkit.enchantments.Enchantment;

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
        return a == b || a.equals(b) || a.conflictsWith(b) || b.conflictsWith(a);
    }

    public static List<Enchantment> getAllEnchants() {
        return UberConfiguration.UberRecord.values()
                .stream()
                .map(UberConfiguration.UberRecord::enchantment)
                .collect(Collectors.toList());
    }
}
