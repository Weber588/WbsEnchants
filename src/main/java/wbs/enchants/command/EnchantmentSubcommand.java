package wbs.enchants.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.definition.EnchantmentDefinition;
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
        builder.then(Commands.argument("enchantment", ArgumentTypes.resourceKey(RegistryKey.ENCHANTMENT))
                .suggests(new EnchantmentSuggestionProvider(this::filter))
                .executes(this::execute)
                .then(Commands.argument("level", IntegerArgumentType.integer())
                        .suggests(new EnchantmentLevelSuggestionProvider())
                        .executes(this::executeLevel)
                )
        );
    }

    protected boolean filter(@Nullable CommandContext<?> context, EnchantmentDefinition enchantment) {
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

    protected Key getEnchantmentKey(CommandContext<CommandSourceStack> context) {
        return context.getArgument("enchantment", TypedKey.class).key();
    }

    protected EnchantmentDefinition getEnchantment(CommandContext<CommandSourceStack> context) {
        Key enchantKey = getEnchantmentKey(context);

        WbsEnchantment customEnchantment = EnchantManager.getCustomFromKey(enchantKey);
        if (customEnchantment != null) {
            return customEnchantment.getDefinition();
        } else {
            return EnchantManager.getExternalDefinition(enchantKey);
        }
    }
}
