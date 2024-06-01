package wbs.enchants.command;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.ConflictEnchantment;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.RomanNumerals;
import wbs.utils.util.string.WbsStrings;

import java.util.List;

public class SubcommandInfo extends EnchantmentSubcommand {
    public SubcommandInfo(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected void onEnchantCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, WbsEnchantment enchant) {
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
        //    sendMessageNoPrefix("Permission: &h" + enchant.getPermission(), sender);
        }

        sendMessageNoPrefix("Description: &h" + enchant.getDescription(), sender);

        List<Enchantment> conflicts = EnchantUtils.getConflictsWith(enchant);
        // Don't show enchants that only exist to conflict (typically curses)
        conflicts.removeIf(check -> check instanceof ConflictEnchantment);
        conflicts.removeIf(enchant::equals);

        if (enchant instanceof ConflictEnchantment conflictEnchant) {
            sendMessageNoPrefix("Conflicts with: &h" + conflictEnchant.getConflictsDescription(), sender);
        } else if (!conflicts.isEmpty()) {
            WbsMessageBuilder builder = plugin.buildMessageNoPrefix("Conflicts with:");

            for (Enchantment conflict : conflicts) {
                builder.append("\n    &h- ");
                if (conflict instanceof UberEnchantment uEnchant) {
                    builder.append(uEnchant.getDisplayName());
                } else {
                    // TODO: Migrate WbsPlugin#append to accept BaseComponent instead of TextComponent to allow
                    //  a TranslatableComponent to be used here.
                    String vanillaName = WbsStrings.capitalizeAll(
                            conflict.getKey().getKey().replaceAll("_", " ")
                    );
                    builder.append("&7" + vanillaName);
                }
            }

            builder.build().send(sender);
        }

        sendMessage(line, sender);
    }
}
