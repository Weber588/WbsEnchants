package wbs.enchants.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.definition.DescribeOption;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.definition.EnchantmentExtension;
import wbs.enchants.definition.EnchantmentWrapper;
import wbs.enchants.type.EnchantmentTypeManager;

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

    public static void addEnchantment(EnchantmentExtension enchant, @NotNull ItemStack item, int level) {
        addEnchantment(enchant.getDefinition(), item, level);
    }
    public static void addEnchantment(EnchantmentWrapper enchant, @NotNull ItemStack item, int level) {
        Enchantment enchantment = enchant.getEnchantment();
        addEnchantment(enchantment, item, level);
    }

    public static void addEnchantment(Enchantment enchantment, @NotNull ItemStack item, int level) {
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            meta.addStoredEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        } else {
            item.addUnsafeEnchantment(enchantment, level);
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

    public static @Nullable WbsEnchantment getAsCustom(Enchantment enchantment) {
        return EnchantManager.getCustomRegistered().stream()
                .filter(check -> check.key().equals(enchantment.getKey()))
                .findFirst()
                .orElse(null);
    }

    public static Component getHoverText(Enchantment enchantment) {
        return getHoverText(enchantment, null);
    }

    public static Component getHoverText(Enchantment enchantment, List<DescribeOption> options) {
        EnchantmentDefinition registeredEnchant = EnchantManager.getFromKey(enchantment.key());
        if (registeredEnchant != null) {
            return registeredEnchant.getHoverText(options);
        }

        return Component.text("Unknown Enchantment.").color(NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD);
    }

    public static boolean isWbsManaged(Enchantment enchantment) {
        return getAsCustom(enchantment) != null;
    }

    public static Component getDisplayName(Enchantment enchant) {
        Component displayName = enchant.description();

        displayName = displayName.applyFallbackStyle(EnchantmentTypeManager.getType(enchant).getColour());

        return displayName;
    }

    public static @NotNull Map<Enchantment, Integer> getEnchants(ItemStack item) {
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            return new HashMap<>(meta.getStoredEnchants());
        }

        return new HashMap<>(item.getEnchantments());
    }

    public static boolean hasEnchants(ItemStack item) {
        return !getEnchants(item).isEmpty();
    }

    public static void addEnchantments(ItemStack item, Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
            addEnchantment(enchantment.getKey(), item, enchantment.getValue());
        }
    }

    public static boolean canEnchant(Enchantment enchantment, ItemStack item) {
        if (!enchantment.canEnchantItem(item)) {
            return false;
        }

        Map<Enchantment, Integer> enchants = getEnchants(item);

        return enchants.keySet().stream().noneMatch(other -> !other.equals(enchantment) && other.conflictsWith(enchantment));
    }
}
