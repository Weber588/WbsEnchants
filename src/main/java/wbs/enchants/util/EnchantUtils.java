package wbs.enchants.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.*;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.definition.EnchantmentExtension;
import wbs.enchants.definition.EnchantmentWrapper;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.string.RomanNumerals;

import java.util.EnumSet;
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
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            meta.addStoredEnchant(enchant.getEnchantment(), level, true);
            item.setItemMeta(meta);
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

    public static @Nullable WbsEnchantment getAsCustom(Enchantment enchantment) {
        return EnchantManager.getCustomRegistered().stream()
                .filter(check -> check.key().equals(enchantment.getKey()))
                .findFirst()
                .orElse(null);
    }

    public static Component getHoverText(Enchantment enchantment) {
        return getHoverText(enchantment, null);
    }

    public static Component getHoverText(Enchantment enchantment, EnumSet<EnchantmentDefinition.DescribeOptions> options) {
        EnchantmentDefinition registeredEnchant = EnchantManager.getFromKey(enchantment.key());
        if (registeredEnchant != null) {
            return registeredEnchant.getHoverText(options);
        }

        if (options == null) {
            options = EnumSet.allOf(EnchantmentDefinition.DescribeOptions.class);
        }

        WbsMessageBuilder builder = WbsEnchants.getInstance().buildMessage("&h&m        &h ")
                .append(getDisplayName(enchantment))
                .append(" &h&m        &h");

        if (options.contains(EnchantmentDefinition.DescribeOptions.MAX_LEVEL)) {
            builder.append("\n&rMax level: &h" + RomanNumerals.toRoman(enchantment.getMaxLevel()) + " (" + enchantment.getMaxLevel() + ")");
        }
        if (options.contains(EnchantmentDefinition.DescribeOptions.TARGET)) {
            // TODO: Find a way to show target tag. Currently can't, since Enchantment only has getSupportedItems
            //  which is a set of supported item keys, not the tag itself.
            //  Either find a way to get that tag directly, or find a way to iterate over all item tags and
            //  find a tag that exactly matches the Enchantment.
        }
        if (options.contains(EnchantmentDefinition.DescribeOptions.DESCRIPTION)) {
            if (enchantment.key().namespace().equals("minecraft")) {
                builder.append("\n&rDescription: &hVanilla Enchantment");
            } else {
                builder.append("\n&rDescription: &hUnknown - Custom Enchantment");
            }
        }

        return builder.toComponent();
    }

    public static boolean isWbsManaged(Enchantment enchantment) {
        return getAsCustom(enchantment) != null;
    }

    public static Component getDisplayName(Enchantment enchant) {
        Component displayName = enchant.description();

        if (!displayName.hasStyling()) {
            displayName = displayName.color(EnchantmentTypeManager.getType(enchant).getColour());
        }

        return displayName;
    }
}
