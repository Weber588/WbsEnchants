package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantManager;
import wbs.enchants.EnchantmentDefinition;
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
    public SubcommandInfo(@NotNull WbsPlugin plugin) {
        super(plugin, "info");
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        plugin.sendMessage("Usage: &h/cench info <enchantment>", sender);
        return 0;
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> context) {
        EnchantmentDefinition definition = getEnchantment(context);
        if (definition == null) {
            plugin.sendMessage("Invalid key: " + context.getArgument("enchantment", Key.class), context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }
        CommandSender sender = context.getSource().getSender();

        int maxLevel = definition.getEnchantment().getMaxLevel();
        if (maxLevel == 0) {
            maxLevel = 1;
        }

        EnchantmentType type = definition.type();

        String line = "=====================================";
        WbsMessageBuilder builder = plugin.buildMessage(line)
                .append("\n&rName: ").append(definition.displayName())
                .append("\n&rType: ").append(type.getNameComponent())
                .append("\n&rMaximum level: &h" + RomanNumerals.toRoman(maxLevel) + " (" + maxLevel + ")")
                .append("\n&rTarget: &h" + definition.targetDescription())
                .append("\n&rDescription: &h" + definition.description());

        if (sender.isOp()) {
            builder.append("\n&rGeneration: &h");

            definition.getGenerationContexts().forEach(genContext -> {
                builder.append("\n\t" + genContext.toString());
            });
        }

        builder.send(sender);

        List<Enchantment> conflicts = EnchantUtils.getConflictsWith(definition.getEnchantment());
        // Don't show enchants that only exist to conflict (typically curses)
        conflicts.removeIf(check -> EnchantUtils.getAsCustom(check) instanceof ConflictEnchantment);
        conflicts.removeIf(other -> definition.key().equals(other.getKey()));

        if (EnchantManager.getFromKey(definition.key()) instanceof ConflictEnchantment conflictEnchant) {
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
                            + ":customenchants info " + definition.key().asString()
                    );
                }
            }

            messageBuilder.build().send(sender);
        }

        plugin.sendMessage(line, sender);
        return Command.SINGLE_SUCCESS;
    }

}
