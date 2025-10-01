package wbs.enchants.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import org.bukkit.Bukkit;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class ItemUtils {
    public static TypedKey<ItemType> getTypedKey(ItemStack item) {
        if (item == null) {
            return null;
        }
        return TypedKey.create(RegistryKey.ITEM, Objects.requireNonNull(item.getType().asItemType()).key());
    }

    @Nullable
    public static ItemStack smeltItem(ItemStack stack) {
        ItemStack result = null;
        int escape = 0;
        for (Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            Recipe recipe;
            try {
                escape++;
                recipe = it.next();
            } catch (IllegalArgumentException ex) {
                if (escape < 100) {
                    throw ex;
                }
                continue;
            }

            if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                RecipeChoice inputChoice = furnaceRecipe.getInputChoice();
                if (inputChoice.test(stack)) {
                    result = furnaceRecipe.getResult();
                    result.setAmount(result.getAmount() * stack.getAmount());
                    break;
                }
            }
        }

        return result;
    }

    public static boolean isBlockingItem(ItemStack item) {
        return item.hasData(DataComponentTypes.BLOCKS_ATTACKS);
    }
}
