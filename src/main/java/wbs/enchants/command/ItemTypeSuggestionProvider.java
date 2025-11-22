package wbs.enchants.command;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.commands.brigadier.KeyedSuggestionProvider;

import java.util.function.BiPredicate;

public class ItemTypeSuggestionProvider implements KeyedSuggestionProvider<ItemType> {
    private final BiPredicate<@Nullable CommandContext<?>, ItemType> filter;

    @Override
    public Iterable<ItemType> getSuggestions(CommandContext<CommandSourceStack> context) {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).stream()
                .filter(itemType -> filter.test(context, itemType))
                .toList();
    }

    public ItemTypeSuggestionProvider(BiPredicate<@Nullable CommandContext<?>, ItemType> filter) {
        this.filter = filter;
    }
}
