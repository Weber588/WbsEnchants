package wbs.enchants.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

public abstract class AbstractBlockSubcommand extends WbsSubcommand {
    public AbstractBlockSubcommand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(
                Commands.argument("block", ArgumentTypes.blockPosition())
                        .executes(this::executeWithBlock)
        );
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            plugin.sendMessage("Usage: &h/" + context.getInput() + " <block>", sender);
            return 0;
        }

        AttributeInstance reachAttribute = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        int range = 5;
        if (reachAttribute != null) {
            range = (int) Math.ceil(reachAttribute.getValue());
        }

        return executeOnBlock(player, player.getTargetBlock(null, range));
    }

    protected int executeWithBlock(CommandContext<CommandSourceStack> context) {
        Block block = context.getArgument("block", Block.class);

        return executeOnBlock(context.getSource().getSender(), block);
    }

    protected abstract int executeOnBlock(CommandSender sender, Block block);
}
