package wbs.enchants.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.EnchantmentDefinition;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

@SuppressWarnings("UnstableApiUsage")
public abstract class EnchantmentSubcommand extends WbsSubcommand {
    public EnchantmentSubcommand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);

        permission = "wbsenchants.command." + label;
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.argument("enchantment", ArgumentTypes.key())
                .suggests(new CustomEnchantmentSuggestionProvider(this::filter))
                .executes(this::execute)
                .then(Commands.argument("level", IntegerArgumentType.integer())
                        .suggests(new EnchantmentLevelSuggestionProvider())
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

    protected EnchantmentDefinition getEnchantment(CommandContext<CommandSourceStack> context) {
        Key enchantKey = context.getArgument("enchantment", Key.class);

        WbsEnchantment customEnchantment = EnchantManager.getFromKey(enchantKey);
        if (customEnchantment != null) {
            return customEnchantment.getDefinition();
        } else {
            return EnchantManager.getExternalDefinition(enchantKey);
        }
    }
}
