package wbs.enchants.command;

import org.bukkit.command.PluginCommand;
import wbs.utils.util.commands.WbsCommand;
import wbs.utils.util.plugin.WbsPlugin;

public class CommandCustomEnchant extends WbsCommand {
    public CommandCustomEnchant(WbsPlugin plugin, PluginCommand command) {
        super(plugin, command);
        addSubcommand(new SubcommandInfo(plugin, "info"), "wbsenchants.command.info");
    }
}
