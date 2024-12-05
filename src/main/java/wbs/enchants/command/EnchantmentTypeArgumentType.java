package wbs.enchants.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchants;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EnchantmentTypeArgumentType implements CustomArgumentType<EnchantmentType, Component> {
    @Override
    public @NotNull EnchantmentType parse(@NotNull StringReader stringReader) throws CommandSyntaxException {
        String search = stringReader.readString();

        WbsEnchants.getInstance().getLogger().info("searching for enchantment type: " + search
                + " (via string reader: " + stringReader + ")");

        Optional<EnchantmentType> found = EnchantmentTypeManager.getRegistered()
                .stream()
                .filter(type -> type.matches(search))
                .findFirst();

        if (found.isPresent()) {
            return found.get();
        } else {
            throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                    () -> "Type not found: " + search + ".");
        }
    }

    @Override
    public @NotNull ArgumentType<Component> getNativeType() {
        return ArgumentTypes.component();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context,
                                                                       @NotNull SuggestionsBuilder builder) {
        for (EnchantmentType type : EnchantmentTypeManager.getRegistered()) {
            builder.suggest(type.getKey().getKey());
        }

        return builder.buildFuture();
    }
}
