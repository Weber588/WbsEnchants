package wbs.enchants.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.plugin.WbsPlugin;

@SuppressWarnings("UnstableApiUsage")
public abstract class EnchantmentSubcommand extends Subcommand {
    public EnchantmentSubcommand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getArgument() {
        return Commands.literal(label)
                .requires(this::canRun)
                .executes(this::executeNoArgs)
                .then(Commands.argument("enchantment", new CustomEnchantArgumentType(this::filter))
                        .executes(this::execute)
                        .then(Commands.argument("level", new EnchantmentLevelArgumentType())
                                .executes(this::executeLevel)
                        )
                );
    }

    protected boolean filter(@Nullable CommandContext<?> context, WbsEnchantment enchantment) {
        return true;
    }

    protected boolean canRun(CommandSourceStack context) {
        return true;
    }

    protected abstract int executeNoArgs(CommandContext<CommandSourceStack> context);

    protected abstract int execute(CommandContext<CommandSourceStack> context);
    protected int executeLevel(CommandContext<CommandSourceStack> context) {
        return execute(context);
    }
}
