package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.ConflictEnchantment;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.RomanNumerals;

import java.util.Comparator;
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

        EnchantmentType type = enchant.getType();

        String line = "=====================================";
        plugin.buildMessage(line)
                .append("\n&rName: &h" + enchant.getDisplayName())
                .append("\n&rType: ").append(type.getNameComponent())
                .append("\n&rMaximum level: &h" + RomanNumerals.toRoman(maxLevel) + " (" + maxLevel + ")")
                .append("\n&rTarget: &h" + enchant.getTargetDescription())
                .append("\n&rDescription: &h" + enchant.getDescription())
                .send(sender);

        List<Enchantment> conflicts = EnchantUtils.getConflictsWith(enchant.getEnchantment());
        // Don't show enchants that only exist to conflict (typically curses)
        conflicts.removeIf(check -> EnchantUtils.getAsCustom(check) instanceof ConflictEnchantment);
        conflicts.removeIf(other -> enchant.getKey().equals(other.getKey()));

        if (enchant instanceof ConflictEnchantment conflictEnchant) {
            plugin.sendMessageNoPrefix("Conflicts with: &h" + conflictEnchant.getConflictsDescription(), sender);
        } else if (!conflicts.isEmpty()) {
            conflicts.sort(Comparator.comparing(Keyed::getKey));

            WbsMessageBuilder messageBuilder = plugin.buildMessageNoPrefix("Conflicts with:");

            for (Enchantment conflict : conflicts) {
                boolean isCustom = EnchantUtils.isWbsManaged(conflict);

                Component hoverText = EnchantUtils.getHoverText(conflict);
                if (isCustom) {
                    hoverText = hoverText.append(Component.text("\n\nClick to view full info!"))
                            .color(plugin.getTextColour());
                }

                messageBuilder.append("\n    &h- ")
                        .append(EnchantUtils.getDisplayName(conflict))
                        .addHoverText(hoverText);

                if (isCustom) {
                    messageBuilder.addClickCommand("/" +
                            WbsEnchants.getInstance().getName().toLowerCase()
                            + ":customenchants info " + enchant.getKey().getKey()
                    );
                }
            }

            messageBuilder.build().send(sender);
        }

        plugin.sendMessage(line, sender);
        return Command.SINGLE_SUCCESS;
    }

}
