package wbs.enchants.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.commands.brigadier.KeyedSuggestionProvider;
import wbs.utils.util.commands.brigadier.argument.WbsWordArgumentType;

import java.util.Optional;

public class EnchantmentTypeArgumentType implements WbsWordArgumentType<EnchantmentType>, KeyedSuggestionProvider<EnchantmentType> {
    @Override
    public @NotNull EnchantmentType parse(@NotNull String asString) throws CommandSyntaxException {
        Optional<EnchantmentType> found = EnchantmentTypeManager.getRegistered()
                .stream()
                .filter(type -> type.matches(asString))
                .findFirst();

        if (found.isPresent()) {
            return found.get();
        } else {
            throw new CommandSyntaxException(
                    CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException(),
                    () -> "Type not found: " + asString + ".");
        }
    }

    @Override
    public Iterable<EnchantmentType> getSuggestions(CommandContext<CommandSourceStack> commandContext) {
        return EnchantmentTypeManager.getRegistered();
    }
}
