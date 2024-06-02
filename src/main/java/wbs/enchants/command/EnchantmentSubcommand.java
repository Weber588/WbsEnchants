package wbs.enchants.command;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class EnchantmentSubcommand extends WbsSubcommand {
    public EnchantmentSubcommand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected final boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        List<WbsEnchantment> enchants = EnchantsSettings.getRegistered();

        if (args.length <= start) {
            sendUsage("<enchantment>", sender, label, args);
            sendMessage("Options: &h" + enchants.stream()
                    .map(WbsEnchantment::getKey)
                    .map(NamespacedKey::getKey)
                    .sorted()
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

            String suggestionString = "";
            if (enchant != null) {
                suggestionString = "Did you mean \"&h" + enchant.getKey().getKey() + "&r\"? ";
            }

            sendMessage("Enchantment not found: \"&h" + enchantmentString + "&r\". " + suggestionString +
                    "Please choose from the following:", sender);

            sendMessage("Options: &h" + enchants.stream()
                    .map(EnchantmentSubcommand::getKeyDisplay)
                    .sorted()
                    .collect(Collectors.joining(", ")), sender);
            return true;
        }

        onEnchantCommand(sender, label, args, start + 1, enchant);
        return true;
    }

    private static String getKeyDisplay(WbsEnchantment enchantment) {
        String keyString = enchantment.getKey().getKey();

        if (enchantment.isCursed()) {
            keyString = "&c" + keyString;
        } else {
            keyString = "&7" + keyString;
        }

        return keyString + "&r";
    }

    protected abstract void onEnchantCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, WbsEnchantment enchant);

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length == start) {
            return EnchantsSettings.getRegistered().stream()
                    .map(WbsEnchantment::getKey)
                    .map(NamespacedKey::getKey)
                    .sorted()
                    .collect(Collectors.toList());
        }

        return new LinkedList<>();
    }
}
