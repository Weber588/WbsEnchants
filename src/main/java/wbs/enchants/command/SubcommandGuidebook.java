package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.guidebook.Guidebook;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandGuidebook extends WbsSubcommand {
    public SubcommandGuidebook(@NotNull WbsPlugin plugin) {
        super(plugin, "guidebook");

        permission = "wbsenchants.command." + label;
    }

    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return Command.SINGLE_SUCCESS;
        }

        plugin.sendMessage("Opening guide book.", sender);
        player.openBook(Guidebook.getGuidebook().toBook());

        return Command.SINGLE_SUCCESS;
    }
}
