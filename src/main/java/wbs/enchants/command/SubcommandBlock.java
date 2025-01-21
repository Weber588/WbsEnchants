package wbs.enchants.command;

import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsCommand;
import wbs.utils.util.plugin.WbsPlugin;

public class SubcommandBlock extends WbsCommand {

    public SubcommandBlock(@NotNull WbsPlugin plugin) {
        super(plugin, "block", "Information relating to blocks in the world.");

        addSubcommands(
                new SubcommandBlockEnchants(plugin),
                new SubcommandBlockDebug(plugin)
        );
    }
}
