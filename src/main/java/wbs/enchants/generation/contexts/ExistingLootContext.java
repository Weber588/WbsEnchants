package wbs.enchants.generation.contexts;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.EnchantmentDefinition;
import wbs.enchants.WbsEnchants;
import wbs.enchants.generation.GenerationContext;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class ExistingLootContext extends GenerationContext {
    private double bookToEnchantedBookChance = 0;
    private int maxEnchantments = 1;
    private double addChance = 0;
    private List<Material> addMaterialOptions = List.of(Material.ENCHANTED_BOOK);
    private boolean clear;

    public ExistingLootContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);

        bookToEnchantedBookChance = section.getDouble("convert-to-enchanted-book-chance", bookToEnchantedBookChance);
        maxEnchantments = section.getInt("max-enchantments", maxEnchantments);
        addChance = section.getDouble("add-chance", addChance);
        clear = section.getBoolean("clear", clear);

        if (section.isList("add-materials")) {
            List<String> materialOptions = section.getStringList("add-materials");
            List<Material> newMaterialOptions = new LinkedList<>();
            for (String matString : materialOptions) {
                Material check = WbsEnums.getEnumFromString(Material.class, matString);

                if (check != null) {
                    newMaterialOptions.add(check);
                } else {
                    WbsEnchants.getInstance().settings.logError("Invalid material: " + matString, 
                            directory + "/add-materials");
                }
            }

            if (!newMaterialOptions.isEmpty()) {
                this.addMaterialOptions = newMaterialOptions;
            }
        }
    }

    @Override
    public void writeToSection(ConfigurationSection section) {
        section.set("convert-to-enchanted-book-chance", bookToEnchantedBookChance);
        section.set("max-enchantments", maxEnchantments);
    }

    protected int tryAddingTo(List<ItemStack> existing) {
        int generated = 0;

        if (addChance > 0 && clear) {
            existing.clear();
        }

        for (ItemStack stack : existing) {
            if (stack == null) {
                continue;
            }
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

            if (definition.tryAdd(stack, generateLevel())) {
                generated++;
                if (generated >= maxEnchantments) {
                    break;
                }
            } else if (convertedToEBook) {
                stack.setType(originalType);
                stack.setAmount(originalAmount);
            }
        }

        if (generated < maxEnchantments) {
            if (WbsMath.chance(addChance)) {
                ItemStack toAdd = new ItemStack(WbsCollectionUtil.getRandom(addMaterialOptions));
                if (definition.tryAdd(toAdd, generateLevel())) {
                    generated++;
                    existing.add(toAdd);
                }
            }
        }

        Collections.shuffle(existing);

        return generated;
    }


}
