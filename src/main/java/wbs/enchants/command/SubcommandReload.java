package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

import java.util.List;

public class SubcommandReload extends Subcommand {
    public SubcommandReload(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getArgument() {
        return Commands.literal("reload").executes(this::execute);
    }

    protected int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        plugin.sendMessage("&6Reloading will not update enchantment definitions! Vanilla attributes like name, cost, " +
                "supported items, max level, and weight will only be updated on a server restart", sender);

        WbsSettings settings = WbsEnchants.getInstance().settings;
        settings.reload();
        List<String> errors = settings.getErrors();
        if (errors.isEmpty()) {
            plugin.sendMessage("&aReload successful!", sender);
        } else {
            plugin.sendMessage("&wThere were " + errors.size() + " config errors. Do &h/" + context.getRootNode().getName() + " errors&w to see them.", sender);
        }

        return Command.SINGLE_SUCCESS;
    }
}
