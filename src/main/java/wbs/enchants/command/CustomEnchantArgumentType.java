package wbs.enchants.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class CustomEnchantArgumentType implements CustomArgumentType<WbsEnchantment, String> {
    @Override
    public @NotNull WbsEnchantment parse(@NotNull StringReader stringReader) throws CommandSyntaxException {
        String[] inputs = stringReader.getRemaining().split(" ");
        // Stop string reader from screaming when we don't read using cursor regularly
        stringReader.setCursor(stringReader.getTotalLength());

        String search = inputs[inputs.length - 1];

        Optional<WbsEnchantment> found = EnchantManager.getRegistered()
                .stream()
                .filter(ench -> ench.matches(search))
                .findFirst();

        if (found.isPresent()) {
            return found.get();
        } else {
            throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                    () -> "Enchantment not found: " + search + ".");
        }
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context,
                                                                       @NotNull SuggestionsBuilder builder) {
        for (WbsEnchantment ench : EnchantManager.getRegistered()) {
            builder.suggest(ench.getKey().getKey());
        }

        return builder.buildFuture();
    }
}
