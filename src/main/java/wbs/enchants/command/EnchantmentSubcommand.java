package wbs.enchants.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.plugin.WbsPlugin;

@SuppressWarnings("UnstableApiUsage")
public abstract class EnchantmentSubcommand extends Subcommand {
    public EnchantmentSubcommand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getArgument() {
        return Commands.literal(label)
                .executes(this::executeNoArgs)
                .then(Commands.argument("enchantment", new CustomEnchantArgumentType())
                        .executes(this::execute)
                );
    }

    protected abstract int executeNoArgs(CommandContext<CommandSourceStack> context);

    protected abstract int execute(CommandContext<CommandSourceStack> context);
}
