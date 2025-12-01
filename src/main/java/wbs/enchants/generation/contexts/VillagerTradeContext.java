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
import wbs.utils.util.WbsKeyed;

import java.util.Map;
import java.util.Random;

public class VillagerTradeContext extends GenerationContext {

    @Nullable
    private Integer villagerLevel = null;
    @Nullable
    private Villager.Profession villagerProfession = null;
    @NotNull
    private String resultString = "minecraft:enchanted_book";
    private int emeraldCostMin = 1;
    private int emeraldCostMax = 64;
    @Nullable
    private String itemCostString = "minecraft:book";
    @Nullable
    private String replaceItemKey = null;

    public VillagerTradeContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);

        if (section.isInt("villager-level")) {
            villagerLevel = section.getInt("villager-level");
            if (villagerLevel < 1 || villagerLevel > 5) {
                villagerLevel = null;
            }
        }

        replaceItemKey = section.getString("replace", replaceItemKey);

        String professionString = section.getString("profession");

        if (professionString != null) {
            villagerProfession = WbsKeyed.getKeyedFromString(Villager.Profession.class, professionString);
        }

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

    private static final Map<Integer, String> LEVEL_NAMES = Map.of(
            1, "Novice",
            2, "Apprentice",
            3, "Journeyman",
            4, "Expert",
            5, "Master"
    );

    @Override
    protected Component describeContext(TextComponent listBreak) {
        TextComponent description;

        if (replaceItemKey == null) {
            description = Component.text("On");
        } else {
            description = Component.text("Replacing " + replaceItemKey + " on");
        }

        if (villagerLevel != null) {
            description = description.append(Component.text(" " + LEVEL_NAMES.get(villagerLevel)));
        }

        if (villagerProfession != null) {
            description = description.append(Component.text(" " + WbsKeyed.toPrettyString(villagerProfession)));
        }

        description = description.append(Component.text(" villager trades: " + chanceToRun() + "%"));

        return description;
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
        if (villagerLevel != null && villager.getVillagerLevel() != villagerLevel) {
            return;
        }

        if (villagerProfession != null && villager.getProfession() != villagerProfession) {
            return;
        }

        ItemStack result = event.getRecipe().getResult();
        if (replaceItemKey != null && !result.isSimilar(Bukkit.getItemFactory().createItemStack(replaceItemKey))) {
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
