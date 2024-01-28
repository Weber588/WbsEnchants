package wbs.enchants.command;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import me.sciguymjm.uberenchant.api.utils.UberUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsKeyed;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.RomanNumerals;
import wbs.utils.util.string.WbsStringify;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SubcommandInfo extends WbsSubcommand {
    public SubcommandInfo(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<WbsEnchantment> enchants = EnchantsSettings.getRegistered();

        if (args.length <= start) {
            sendUsage("<enchantment>", sender, label, args);
            sendMessage("Options: &h" + enchants.stream()
                    .map(WbsEnchantment::getKey)
                    .map(NamespacedKey::getKey)
                    .collect(Collectors.joining(", ")), sender);
            return true;
        }

        String enchantmentString = args[start];

        WbsEnchantment enchant = enchants.stream()
                .filter(check -> check.matches(enchantmentString))
                .findFirst()
                .orElse(null);

        if (enchant == null) {
            enchant = enchants.stream()
                    .filter(check -> check.looselyMatches(enchantmentString))
                    .findFirst()
                    .orElse(null);

            if (enchant == null) {
                sendMessage("Enchantment not found: \"" + enchantmentString + "\". " +
                        "Please choose from the following:", sender);

                sendMessage("Options: &h" + enchants.stream()
                        .map(WbsEnchantment::getKey)
                        .map(NamespacedKey::getKey)
                        .collect(Collectors.joining(", ")), sender);
                return true;
            }
        }

        int maxLevel = enchant.getMaxLevel();
        if (maxLevel == 0) {
            maxLevel = 1;
        }

        String line = "=====================================";
        sendMessage(line, sender);

        sendMessageNoPrefix("Name: &h" + enchant.getDisplayName(), sender);
        sendMessageNoPrefix("Maximum level: &h" + RomanNumerals.toRoman(maxLevel) + " (" + maxLevel + ")", sender);
        sendMessageNoPrefix("Target: &h" + enchant.getTargetDescription(), sender);
        if (enchant.getPermission() != null) {
            sendMessageNoPrefix("Permission: &h" + enchant.getPermission(), sender);
        }

        sendMessageNoPrefix("Description: &h" + enchant.getDescription(), sender);

        List<Enchantment> conflicts = EnchantUtils.getConflictsWith(enchant);
        if (!conflicts.isEmpty()) {
            WbsMessageBuilder builder = plugin.buildMessage("Conflicts with:");

            for (Enchantment conflict : conflicts) {
                builder.append("\n\t&h- ");
                if (conflict instanceof UberEnchantment uEnchant) {
                    builder.append(uEnchant.getDisplayName());
                } else {
                    // TODO: Migrate WbsPlugin#append to accept BaseComponent instead of TextComponent to allow
                    // a TranslatableComponent to be used here.
                    String vanillaName = WbsStrings.capitalizeAll(
                            conflict.getKey().getKey().replaceAll("_", " ")
                    );
                    builder.append("&7" + vanillaName);
                }
            }
        }

        sendMessage(line, sender);

        return true;
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length == start) {
            return EnchantsSettings.getRegistered().stream()
                    .map(WbsEnchantment::getKey)
                    .map(NamespacedKey::getKey)
                    .collect(Collectors.toList());
        }

        return new LinkedList<>();
    }
}
