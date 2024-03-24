package wbs.enchants.util;

import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import wbs.enchants.WbsEnchantment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class EnchantUtils {

    public static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            return meta.getStoredEnchants();
        } else {
            return stack.getEnchantments();
        }
    }

    // TODO: Config option that makes it also handle vanilla enchants, so levels above 10 don't bjork
    public static boolean addEnchantment(Enchantment enchantment, ItemStack stack, int level) {
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta) {
            return addStoredEnchantment(enchantment, stack, level);
        }

        if (enchantment instanceof WbsEnchantment customEnchant) {
            return addEnchantment(customEnchant, stack, level);
        }

        stack.addEnchantment(enchantment, level);
        return true;
    }

    public static boolean addStoredEnchantment(Enchantment enchantment, ItemStack stack, int level) {
        if (!(stack.getItemMeta() instanceof EnchantmentStorageMeta meta)) {
            return false;
        }

        if (enchantment instanceof WbsEnchantment customEnchant) {
            return addStoredEnchantment(customEnchant, stack, level);
        }

        return meta.addStoredEnchant(enchantment, level, true);
    }

    public static boolean addEnchantment(WbsEnchantment enchantment, ItemStack stack, int level) {
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta) {
            return addStoredEnchantment(enchantment, stack, level);
        }

        addLore(stack, enchantment, level);

        stack.addUnsafeEnchantment(enchantment, level);

        return true;
    }

    public static boolean addStoredEnchantment(WbsEnchantment enchantment, ItemStack stack, int level) {
        if (!(stack.getItemMeta() instanceof EnchantmentStorageMeta meta)) {
            return false;
        }

        addLore(stack, enchantment, level);

        return meta.addStoredEnchant(enchantment, level, true);
    }

    private static void addLore(ItemStack stack, Enchantment enchantment, int level) {
        int currentLevel = stack.getEnchantmentLevel(enchantment);

        List<Component> lore = stack.getItemMeta().lore();
        int index = -1;

        if (lore == null) {
            lore = new LinkedList<>();
        } else {
            Predicate<Component> predicate = line -> line.equals(enchantment.displayName(currentLevel));
            for (int i = 0; i < lore.size(); i++) {
                if (predicate.test(lore.get(i))) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                lore.removeIf(predicate);
            }
        }

        Component newLine = enchantment.displayName(level);

        if (index == -1) {
            lore.add(0, newLine);
        } else {
            lore.add(index, newLine);
        }

        stack.lore(lore);
    }
}
