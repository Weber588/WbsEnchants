package wbs.enchants.generation.contexts;

import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.generation.GenerationContext;
import wbs.utils.util.WbsMath;

import java.util.Collections;
import java.util.List;

public abstract class ExistingLootContext extends GenerationContext {
    private double bookToEnchantedBookChance = 0;
    private int maxEnchantments = 1;

    public ExistingLootContext(String key, WbsEnchantment enchantment, ConfigurationSection section, String directory) {
        super(key, enchantment, section, directory);

        bookToEnchantedBookChance = section.getDouble("convert-to-enchanted-book-chance", bookToEnchantedBookChance);
        maxEnchantments = section.getInt("max-enchantments", maxEnchantments);
    }

    @Override
    public void writeToSection(ConfigurationSection section) {
        section.set("convert-to-enchanted-book-chance", bookToEnchantedBookChance);
        section.set("max-enchantments", maxEnchantments);
    }

    protected int tryAddingTo(List<ItemStack> existing) {
        int generated = 0;
        for (ItemStack stack : existing) {
            Material originalType = stack.getType();
            int originalAmount = stack.getAmount();
            boolean convertedToEBook = false;
            if (originalType == Material.BOOK) {
                if (WbsMath.chance(bookToEnchantedBookChance)) {
                    convertedToEBook = true;
                    stack.setType(Material.ENCHANTED_BOOK);
                    stack.setAmount(1);
                }
            }

            if (enchantment.tryAdd(stack, generateLevel())) {
                generated++;
                if (generated >= maxEnchantments) {
                    break;
                }
            } else if (convertedToEBook) {
                stack.setType(originalType);
                stack.setAmount(originalAmount);
            }
        }

        Collections.shuffle(existing);

        return generated;
    }
}
