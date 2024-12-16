package wbs.enchants.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class EnchantmentLevelArgumentType implements CustomArgumentType<Integer, Integer> {
    @Override
    public @NotNull Integer parse(@NotNull StringReader stringReader) throws CommandSyntaxException {
        return stringReader.readInt();
    }

    @Override
    public @NotNull ArgumentType<Integer> getNativeType() {
        return IntegerArgumentType.integer(0);
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context,
                                                                       @NotNull SuggestionsBuilder builder) {
        WbsEnchantment enchantment = context.getArgument("enchantment", WbsEnchantment.class);

        for (int i = 1; i <= Math.max(enchantment.getMaxLevel(), 1); i++) {
            builder.suggest(i);
        }

        return builder.buildFuture();
    }
}
