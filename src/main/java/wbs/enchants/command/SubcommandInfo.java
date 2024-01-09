package wbs.enchants.command;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.RomanNumerals;

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
