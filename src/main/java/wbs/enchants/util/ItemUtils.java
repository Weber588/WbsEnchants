package wbs.enchants.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class ItemUtils {
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
}
