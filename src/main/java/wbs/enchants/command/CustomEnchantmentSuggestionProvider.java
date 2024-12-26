package wbs.enchants.command;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.commands.brigadier.KeyedSuggestionProvider;

import java.util.function.BiPredicate;

public class CustomEnchantmentSuggestionProvider extends KeyedSuggestionProvider<WbsEnchantment> {
    private final BiPredicate<@Nullable CommandContext<?>, WbsEnchantment> filter;

    public CustomEnchantmentSuggestionProvider() {
        this((a, b) -> true);
    }

    @Override
    public Iterable<WbsEnchantment> getSuggestions(CommandContext<CommandSourceStack> context) {
        return EnchantManager.getRegistered().stream().filter(ench -> filter.test(context, ench)).toList();
    }

    public CustomEnchantmentSuggestionProvider(BiPredicate<@Nullable CommandContext<?>, WbsEnchantment> filter) {
        this.filter = filter;
    }
}
