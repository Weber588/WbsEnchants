package wbs.enchants.command;

import org.bukkit.command.PluginCommand;
import wbs.utils.util.commands.WbsCommand;
import wbs.utils.util.plugin.WbsPlugin;

public class CommandCustomEnchant extends WbsCommand {
    public CommandCustomEnchant(WbsPlugin plugin, PluginCommand command) {
        super(plugin, command);
        addSubcommand(new SubcommandInfo(plugin, "info"), "wbsenchants.command.info");
        addSubcommand(new SubcommandList(plugin, "list"), "wbsenchants.command.info");
        addSubcommand(new SubcommandAdd(plugin, "add"), "wbsenchants.command.add");
        addSubcommand(new SubcommandRemove(plugin, "remove"), "wbsenchants.command.remove");
    }
}
