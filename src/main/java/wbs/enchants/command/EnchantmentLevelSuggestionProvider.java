package wbs.enchants.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.registry.TypedKey;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.definition.EnchantmentDefinition;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class EnchantmentLevelSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private static @Nullable EnchantmentDefinition getEnchantment(CommandContext<CommandSourceStack> context) {
        TypedKey<?> enchantKey = context.getArgument("enchantment", TypedKey.class);

        return EnchantManager.getFromKey(enchantKey.key());
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        // Need to use getLastChild to get parent that actually contains the arguments -- see PaperMC issue 11384
        // https://github.com/PaperMC/Paper/issues/11384
        EnchantmentDefinition enchantment = getEnchantment(context.getLastChild());

        if (enchantment != null) {
            for (int i = 1; i <= Math.max(enchantment.maxLevel(), 1); i++) {
                builder.suggest(i);
            }
        }

        return builder.buildFuture();
    }
}
