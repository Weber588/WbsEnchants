package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.plugin.WbsSettings;

import java.util.List;

public class SubcommandErrors extends Subcommand {
    public SubcommandErrors(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getArgument() {
        return Commands.literal("errors")
                .executes(this::execute)
                .then(Commands.argument("page", ArgumentTypes.integerRange())
                        .executes(this::executeWithPage)
                );
    }

    protected int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        return showErrors(sender, 1);
    }
    protected int executeWithPage(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        int page = context.getArgument("page", Integer.class);

        return showErrors(sender, page);
    }

    protected int showErrors(CommandSender sender, int page) {
        WbsSettings settings = WbsEnchants.getInstance().settings;
        List<String> errors = settings.getErrors();
        if (errors.isEmpty()) {
            plugin.sendMessage("&aThere were no errors in the last reload.", sender);
        } else {
            page--;
            int entriesPerPage = 5;
            int pages = errors.size() / entriesPerPage;
            if (errors.size() % entriesPerPage != 0) {
                pages++;
            }

            plugin.sendMessage("Displaying page " + (page + 1) + "/" + pages + ":", sender);
            int index = 0;

            for (String error : errors) {
                index++;
                if (index > (page * entriesPerPage) && index <= (page + 1) * entriesPerPage) {
                    plugin.sendMessage("&6" + index + ") &w" + error, sender);
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
