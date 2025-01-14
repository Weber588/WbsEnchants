package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.definition.EnchantmentDefinition.DescribeOptions;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.WbsSuggestionProvider;
import wbs.utils.util.commands.brigadier.argument.WbsStringArgumentType;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandList extends WbsSubcommand {
    private static final EnumSet<DescribeOptions> DESCRIBE_OPTIONS = EnumSet.of(
            DescribeOptions.TYPE,
            DescribeOptions.DESCRIPTION,
            DescribeOptions.MAX_LEVEL,
            DescribeOptions.TARGET
    );

    public SubcommandList(@NotNull WbsPlugin plugin) {
        super(plugin, "list");

        permission = "wbsenchants.command." + label;
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder
                .then(Commands.argument("namespace", WbsStringArgumentType.word())
                        .suggests(WbsSuggestionProvider.getStatic(EnchantManager.getNamespaces()))
                        .executes(this::execute)
                );
    }

    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        return execute(sender, null);
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        String namespace = context.getArgument("namespace", String.class);

        return execute(sender, namespace);
    }

    private int execute(CommandSender sender, @Nullable String namespace) {
        if (namespace != null) {
            sendEnchantmentList(sender, namespace);
        } else {
            for (String autoNamespace : EnchantManager.getNamespaces()) {
                sendEnchantmentList(sender, autoNamespace);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private void sendEnchantmentList(CommandSender sender, @NotNull String namespace) {
        List<EnchantmentDefinition> enchants = EnchantManager.getAllKnownDefinitions()
                .stream()
                .filter(def -> def.key().namespace().equalsIgnoreCase(namespace))
                .sorted()
                .collect(Collectors.toList());

        if (enchants.isEmpty()) {
            plugin.sendMessage("No enchantments found!", sender);
            return;
        }

        enchants.sort(EnchantmentDefinition::compareTo);

        Component opening = Component.text(namespace).append(Component.text(" enchantments:\n")).color(plugin.getTextColour());

        WbsMessageBuilder builder = plugin.buildMessage("").append(opening);

        EnchantmentDefinition first = enchants.getFirst();
        enchants.removeFirst();
        builder.append(first.interactiveDisplay(DESCRIBE_OPTIONS));

        enchants.forEach(enchant -> {
            builder.append("&r, ");
            builder.append(enchant.interactiveDisplay(DESCRIBE_OPTIONS));
        });

        builder.build().send(sender);
    }
}
