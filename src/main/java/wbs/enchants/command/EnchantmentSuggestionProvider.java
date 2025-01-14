package wbs.enchants.command;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.utils.util.commands.brigadier.KeyedSuggestionProvider;

import java.util.function.BiPredicate;

public class EnchantmentSuggestionProvider implements KeyedSuggestionProvider<EnchantmentDefinition> {
    private final BiPredicate<@Nullable CommandContext<?>, EnchantmentDefinition> filter;

    @Override
    public Iterable<EnchantmentDefinition> getSuggestions(CommandContext<CommandSourceStack> context) {
        return EnchantManager.getAllKnownDefinitions().stream().filter(definition -> filter.test(context, definition)).toList();
    }

    public EnchantmentSuggestionProvider(BiPredicate<@Nullable CommandContext<?>, EnchantmentDefinition> filter) {
        this.filter = filter;
    }
}
