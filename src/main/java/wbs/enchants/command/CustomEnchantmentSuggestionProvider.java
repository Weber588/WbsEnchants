package wbs.enchants.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;

public class CustomEnchantmentSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    private final BiPredicate<@Nullable CommandContext<?>, WbsEnchantment> filter;

    public CustomEnchantmentSuggestionProvider() {
        this((a, b) -> true);
    }
    public CustomEnchantmentSuggestionProvider(BiPredicate<@Nullable CommandContext<?>, WbsEnchantment> filter) {
        this.filter = filter;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext context, SuggestionsBuilder builder) throws CommandSyntaxException {
        for (WbsEnchantment ench : EnchantManager.getRegistered()) {
            if (filter.test(context, ench) && shouldSuggest(ench.getKey(), builder)) {
                builder.suggest(ench.getKey().asString());
            }
        }

        return builder.buildFuture();
    }

    private boolean shouldSuggest(Key key, SuggestionsBuilder builder) {
        WbsEnchants.getInstance().getLogger().info("shouldSuggest " + key.asString() + " for " + builder.getRemainingLowerCase() + "?\n" +
                "(Others: getInput: " + builder.getInput() + "; getStart: " + builder.getStart());
        if (key.asString().toLowerCase().startsWith(builder.getRemainingLowerCase())) {
            return true;
        }
        if (key.value().toLowerCase().startsWith(builder.getRemainingLowerCase())) {
            return true;
        }

        return false;
    }
}
