package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.ConflictEnchantment;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.RomanNumerals;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandInfo extends EnchantmentSubcommand {
    public SubcommandInfo(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        plugin.sendMessage("Usage: &h/cench info <enchantment>", sender);
        return 0;
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> context) {
        WbsEnchantment enchant = context.getArgument("enchantment", WbsEnchantment.class);
        CommandSender sender = context.getSource().getSender();

        int maxLevel = enchant.getEnchantment().getMaxLevel();
        if (maxLevel == 0) {
            maxLevel = 1;
        }

        String line = "=====================================";
        plugin.sendMessage(line, sender);

        plugin.sendMessageNoPrefix("Name: &h" + enchant.getDefaultDisplayName(), sender);
        plugin.sendMessageNoPrefix("Maximum level: &h" + RomanNumerals.toRoman(maxLevel) + " (" + maxLevel + ")", sender);
        plugin.sendMessageNoPrefix("Target: &h" + enchant.getTargetDescription(), sender);
        if (enchant.getPermission() != null) {
            //    sendMessageNoPrefix("Permission: &h" + enchant.getPermission(), sender);
        }

        plugin.sendMessageNoPrefix("Description: &h" + enchant.getDescription(), sender);

        List<Enchantment> conflicts = EnchantUtils.getConflictsWith(enchant.getEnchantment());
        // Don't show enchants that only exist to conflict (typically curses)
        conflicts.removeIf(check -> check instanceof ConflictEnchantment);
        conflicts.removeIf(other -> enchant.getKey().equals(other.getKey()));

        if (enchant instanceof ConflictEnchantment conflictEnchant) {
            plugin.sendMessageNoPrefix("Conflicts with: &h" + conflictEnchant.getConflictsDescription(), sender);
        } else if (!conflicts.isEmpty()) {
            WbsMessageBuilder messageBuilder = plugin.buildMessageNoPrefix("Conflicts with:");

            for (Enchantment conflict : conflicts) {
                messageBuilder.append("\n    &h- ");

                // Go with 5, known to be always " V" so we can just scrub it
                String displayName = conflict.displayName(5).toString();
                displayName = displayName.substring(0, displayName.length() - 2);
                messageBuilder.append("&7" + displayName);
            }

            messageBuilder.build().send(sender);
        }

        plugin.sendMessage(line, sender);
        return Command.SINGLE_SUCCESS;
    }
}
