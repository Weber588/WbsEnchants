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
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.type.EnchantmentType;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandList extends WbsSubcommand {
    public SubcommandList(@NotNull WbsPlugin plugin) {
        super(plugin, "list");

        permission = "wbsenchants.command." + label;
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder
                .then(Commands.argument("type", new EnchantmentTypeArgumentType())
                        .executes(this::execute)
                );
    }

    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        return execute(sender, null);
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        EnchantmentType type = context.getArgument("type", EnchantmentType.class);

        return execute(sender, type);
    }

    private int execute(CommandSender sender, @Nullable EnchantmentType type) {
        List<WbsEnchantment> enchants = EnchantManager.getRegistered()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        if (enchants.isEmpty()) {
            plugin.sendMessage("No enchantments enabled!", sender);
            return 0;
        }

        if (type != null) {
            enchants = enchants.stream().filter(enchant -> enchant.getType() == type).collect(Collectors.toList());
        }

        if (enchants.isEmpty()) {
            plugin.sendMessage("No enchantments of type " + type.getName() + " are enabled!", sender);
            return 0;
        }

        enchants.sort(WbsEnchantment::compareTo);

        Component opening;
        if (type != null) {
            opening = type.getNameComponent().append(Component.text(" enchantments:"));
        } else {
            opening = Component.text("Enchantments:");
        }

        WbsMessageBuilder builder = plugin.buildMessage("").append(opening.color(plugin.getTextColour())).append("\n");

        WbsEnchantment first = enchants.getFirst();
        enchants.removeFirst();
        appendEnchant(builder, first);

        enchants.forEach(enchant -> {
            builder.append("&r, ");
            appendEnchant(builder, enchant);
        });

        builder.build().send(sender);

        return Command.SINGLE_SUCCESS;
    }

    private static void appendEnchant(WbsMessageBuilder builder, WbsEnchantment enchant) {
        builder.append(enchant.displayName())
                .addHoverText(enchant.getHoverText().append(
                        Component.text("\n\nClick to view full info!")
                                .color(WbsEnchants.getInstance().getTextColour())
                        )
                ).addClickCommand("/" +
                        WbsEnchants.getInstance().getName().toLowerCase()
                                + ":customenchants info " + enchant.getKey().asString()
                );
    }
}
