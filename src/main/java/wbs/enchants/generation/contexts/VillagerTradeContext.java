package wbs.enchants.generation.contexts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.util.EnchantUtils;

import java.util.Random;

public class VillagerTradeContext extends GenerationContext {

    private int villagerLevel = 1;
    @NotNull
    private String resultString = "minecraft:enchanted_book";
    private int emeraldCostMin = 1;
    private int emeraldCostMax = 64;
    @Nullable
    private String itemCostString = "minecraft:book";

    public VillagerTradeContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);

        villagerLevel = section.getInt("villager-level", villagerLevel);
        resultString = section.getString("result", resultString);

        ConfigurationSection emeraldCostSection = section.getConfigurationSection("emerald-cost");
        if (emeraldCostSection != null) {
            emeraldCostMin = emeraldCostSection.getInt("min", emeraldCostMin);
            emeraldCostMax = emeraldCostSection.getInt("max", emeraldCostMax);
        } else if (section.isInt("emerald-cost")) {
            emeraldCostMin = section.getInt("emerald-cost", emeraldCostMin);
            emeraldCostMax = section.getInt("emerald-cost", emeraldCostMax);
        }

        if (emeraldCostMin > emeraldCostMax) {
            int max = emeraldCostMin;
            emeraldCostMin = emeraldCostMax;
            emeraldCostMax = max;
        }

        String itemCostString = section.getString("item-cost");
        if (itemCostString != null) {
            this.itemCostString = itemCostString;
        } else {
            if (!resultString.contains("enchanted_book")) {
                this.itemCostString = null;
            }
        }
    }

    @Override
    public void writeToSection(ConfigurationSection section) {
        section.set("villager-level", villagerLevel);
        section.set("result", resultString);
        // TODO
    }

    @Override
    protected int getDefaultChance() {
        return 25;
    }

    @Override
    protected Component describeContext(TextComponent listBreak) {
        return Component.text("On villager trades: " + chanceToRun() + "%");
    }

    @EventHandler
    public void onTradeAcquireEvent(VillagerAcquireTradeEvent event) {
        if (!shouldRun()) {
            return;
        }

        AbstractVillager abstractVillager = event.getEntity();

        if (!(abstractVillager instanceof Villager villager)) {
            return;
        }

        if (!meetsAllConditions(villager, villager.getLocation().getBlock(), villager.getLocation(), null)) {
            return;
        }

        // nms Villager#increaseMerchantCareer increases level BEFORE acquiring trades -- reliable
        if (villager.getVillagerLevel() != villagerLevel) {
            return;
        }

        MerchantRecipe recipe = buildRecipe(event);
        event.setRecipe(recipe);
    }

    private @NotNull MerchantRecipe buildRecipe(VillagerAcquireTradeEvent event) {
        ItemStack result = Bukkit.getItemFactory().createItemStack(resultString);
        EnchantUtils.addEnchantment(definition, result, generateLevel());

        MerchantRecipe recipe = new MerchantRecipe(result, 1);

        MerchantRecipe replace = event.getRecipe();

        // TODO: Add more configurability over recipe details
        recipe.setDemand(replace.getDemand());
        recipe.setExperienceReward(replace.hasExperienceReward());
        recipe.setIgnoreDiscounts(replace.shouldIgnoreDiscounts());
        recipe.setVillagerExperience(replace.getVillagerExperience());
        recipe.setSpecialPrice(replace.getSpecialPrice());
        recipe.setPriceMultiplier(replace.getPriceMultiplier());

        if (emeraldCostMin > 0) {
            recipe.addIngredient(ItemStack.of(Material.EMERALD, new Random().nextInt(emeraldCostMin, emeraldCostMax)));
        }
        if (itemCostString != null) {
            recipe.addIngredient(Bukkit.getItemFactory().createItemStack(itemCostString));
        }

        return recipe;
    }

    @Override
    public String toString() {
        return "VillagerTradeContext{" +
                "item=" + resultString +
                ", villagerLevel=" + villagerLevel +
                ", enchantment=" + definition +
                ", conditions=" + conditions +
                ", key='" + key + '\'' +
                '}';
    }
}
