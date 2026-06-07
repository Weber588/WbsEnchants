package wbs.enchants.generation.contexts;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantsBootstrapSettings;
import wbs.enchants.WbsEnchants;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.string.WbsStrings;

import java.util.Map;
import java.util.Random;

public class VillagerTradeContext extends GenerationContext {

    @Nullable
    private Integer villagerLevel = null;
    @Nullable
    private final String villagerProfession;
    @NotNull
    private String resultString = "minecraft:enchanted_book";
    private int emeraldCostMin = 1;
    private int emeraldCostMax = 64;
    @Nullable
    private String itemCostString = "minecraft:book";
    @Nullable
    private String replaceItemKey = null;
    private final boolean requireWanderingTrader;
    private final boolean includeWanderingTrader;

    public VillagerTradeContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);

        if (section.isInt("villager-level")) {
            villagerLevel = section.getInt("villager-level");
            if (villagerLevel < 1 || villagerLevel > 5) {
                villagerLevel = null;
                EnchantsBootstrapSettings.getInstance().logError("Villager level must be between 1 and 5 (inclusive)", directory + "/villager-level");
            }
        }
        villagerProfession = section.getString("villager-profession");

        replaceItemKey = section.getString("replace", replaceItemKey);


        requireWanderingTrader = section.getBoolean("require-wandering-trader", false);

        if (requireWanderingTrader && (villagerLevel != null || villagerProfession != null)) {
            throw new InvalidConfigurationException("require-wandering-trader and villager-level/villager-profession cannot both be set.", directory);
        }

        includeWanderingTrader = section.getBoolean("include-wandering-trader", false);

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
            ItemStack stack;
            try {
                stack = Bukkit.getItemFactory().createItemStack(replaceItemKey);
                description = Component.text("Replacing ").append(stack.effectiveName().style(Style.empty())).append(Component.text(" on"));
            } catch (IllegalArgumentException ex) {
                description = Component.text("Replacing " + replaceItemKey + " on");
            }
        }

        if (villagerLevel != null) {
            description = description.append(Component.text(" " + LEVEL_NAMES.get(villagerLevel)));
        }

        if (villagerProfession != null) {
            description = description.append(Component.text(" " + WbsStrings.capitalizeAll(villagerProfession)));
        }

        if (requireWanderingTrader) {
            description = description.append(Component.text(" wandering trader"));
        } else if (includeWanderingTrader) {
            description = description.append(Component.text(" villager/wandering trader"));
        } else {
            description = description.append(Component.text(" villager"));
        }

        description = description.append(Component.text(" trades: " + chanceToRun() + "%"));

        return description;
    }

    @EventHandler
    public void onTradeAcquireEvent(VillagerAcquireTradeEvent event) {
        if (!shouldRun()) {
            return;
        }

        AbstractVillager abstractVillager = event.getEntity();

        if (!meetsAllConditions(abstractVillager, abstractVillager.getLocation().getBlock(), abstractVillager.getLocation(), null)) {
            return;
        }

        if (abstractVillager instanceof WanderingTrader) {
            if (!includeWanderingTrader && !requireWanderingTrader) {
                return;
            }
        } else {
            if (requireWanderingTrader) {
                return;
            }
        }

        if (abstractVillager instanceof Villager villager && (villagerLevel != null || villagerProfession != null)) {
            // nms Villager#increaseMerchantCareer increases level BEFORE acquiring trades -- reliable
            if (villagerLevel != null && villager.getVillagerLevel() != villagerLevel) {
                return;
            }

            if (villagerProfession != null) {
                NamespacedKey professionKey = NamespacedKey.fromString(villagerProfession);
                if (professionKey != null) {
                    Villager.Profession profession = RegistryAccess.registryAccess()
                            .getRegistry(RegistryKey.VILLAGER_PROFESSION)
                            .get(professionKey);

                    if (profession != null && villager.getProfession() != profession) {
                        return;
                    }
                } else {
                    WbsEnchants.getInstance().getLogger().warning("Profession key was not parseable! " + villagerProfession);
                }
            }
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

        MerchantRecipe recipe = buildMerchantRecipe(event, result);

        if (emeraldCostMin > 0) {
            recipe.addIngredient(ItemStack.of(Material.EMERALD, new Random().nextInt(emeraldCostMin, emeraldCostMax)));
        }
        if (itemCostString != null) {
            recipe.addIngredient(Bukkit.getItemFactory().createItemStack(itemCostString));
        }

        return recipe;
    }

    private static @NotNull MerchantRecipe buildMerchantRecipe(VillagerAcquireTradeEvent event, ItemStack result) {
        MerchantRecipe recipe = new MerchantRecipe(result, 1);

        MerchantRecipe replace = event.getRecipe();

        // TODO: Add more configurability over recipe details
        recipe.setDemand(replace.getDemand());
        recipe.setExperienceReward(replace.hasExperienceReward());
        recipe.setIgnoreDiscounts(replace.shouldIgnoreDiscounts());
        recipe.setVillagerExperience(replace.getVillagerExperience());
        recipe.setSpecialPrice(replace.getSpecialPrice());
        recipe.setPriceMultiplier(replace.getPriceMultiplier());

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
