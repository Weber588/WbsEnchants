package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.Style;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.EnumSet;
import java.util.List;

import static wbs.enchants.definition.EnchantmentDefinition.DescribeOptions;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandInfo extends EnchantmentSubcommand {
    public SubcommandInfo(@NotNull WbsPlugin plugin) {
        super(plugin, "info");
    }

    EnumSet<DescribeOptions> DESCRIBE_OPTIONS = EnumSet.of(
            DescribeOptions.MAX_LEVEL,
            DescribeOptions.TARGET,
            DescribeOptions.TYPE,
            DescribeOptions.GENERATION,
            DescribeOptions.CONFLICTS,
            DescribeOptions.DESCRIPTION
    );

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        plugin.sendMessage("Usage: &h/cench info <enchantment>", sender);
        return 0;
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        EnchantmentDefinition definition = getEnchantment(context);
        if (definition == null) {
            plugin.sendMessage("Enchantment not found for key &h\"" + getEnchantmentKey(context) + "\".", sender);
            return Command.SINGLE_SUCCESS;
        }

        List<Component> description = definition.getDetailComponents(DESCRIBE_OPTIONS, true);

        String line = "=====================================";
        WbsMessageBuilder builder = plugin.buildMessage(line + "\n");

        JoinConfiguration joinConfig = JoinConfiguration.builder()
                .separator(Component.text("\n"))
                .parentStyle(Style.style(plugin.getTextColour()))
                .build();

        Component mergedDescription = Component.join(joinConfig, description)
                .color(plugin.getTextColour());

        builder.append(mergedDescription)
                .append("\n" + plugin.prefix + " " + plugin.getColour() + line)
                        .send(sender);

        plugin.sendMessage(line, sender);
        return Command.SINGLE_SUCCESS;
    }

}
