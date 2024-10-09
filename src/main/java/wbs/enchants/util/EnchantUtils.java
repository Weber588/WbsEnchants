package wbs.enchants.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EnchantUtils {
    public static List<Enchantment> getConflictsWith(Enchantment enchant) {
        return getConflictsWith(enchant, getAllEnchants());
    }

    public static List<Enchantment> getConflictsWith(Enchantment enchant, List<Enchantment> toCheck) {
        return toCheck.stream()
                .filter(check -> directlyConflictsWith(enchant, check))
                .collect(Collectors.toList());
    }

    public static boolean directlyConflictsWith(Enchantment a, Enchantment b) {
        return a == b || a.equals(b) || a.getKey().equals(b.getKey()) || a.conflictsWith(b) || b.conflictsWith(a);
    }

    public static List<Enchantment> getAllEnchants() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream().toList();
    }

    public static void addEnchantment(WbsEnchantment enchant, @NotNull ItemStack item, int level) {
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            meta.addStoredEnchant(enchant.getEnchantment(), level, true);
        } else {
            item.addUnsafeEnchantment(enchant.getEnchantment(), level);
        }
    }

    public static Map<Enchantment, Integer> getStoredEnchantments(ItemStack item) {
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            return meta.getStoredEnchants();
        }

        return new HashMap<>();
    }

    public static boolean isCurse(Enchantment enchant) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                .getTag(EnchantmentTagKeys.CURSE).contains(TypedKey.create(RegistryKey.ENCHANTMENT, enchant.getKey()));
    }
}
