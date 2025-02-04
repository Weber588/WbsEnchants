package wbs.enchants.generation.contexts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.WbsEnchants;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.util.EnchantUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VillagerTradeContext extends GenerationContext {

    private final List<String> replaceableEnchants;

    public VillagerTradeContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);

        replaceableEnchants = section.getStringList("replaceable").stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    @Override
    public void writeToSection(ConfigurationSection section) {
        section.set("replaceable", replaceableEnchants);
    }

    @Override
    protected int getDefaultChance() {
        // Amount of enchants (including custom ones in case they're available through trading) minus 2 for
        // Soul Speed and Swift Sneak, which may not be obtained through this method.
        return (int) (100.0 / (EnchantUtils.getAllEnchants().size() - 2));
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

        MerchantRecipe recipe = event.getRecipe();
        ItemStack result = recipe.getResult();
        if (!(result.getItemMeta() instanceof EnchantmentStorageMeta meta)) {
            return;
        }

        AbstractVillager abstractVillager = event.getEntity();

        if (!(abstractVillager instanceof Villager villager)) {
            return;
        }

        if (!meetsAllConditions(villager, villager.getLocation().getBlock(), villager.getLocation(), null)) {
            return;
        }

        Map<Enchantment, Integer> storedEnchants = meta.getStoredEnchants();

        Enchantment toReplace = storedEnchants.keySet()
                .stream()
                .filter(ench -> replaceableEnchants.isEmpty() || replaceableEnchants.contains(ench.getKey().toString().toLowerCase()))
                .findAny()
                .orElse(null);

        if (toReplace != null) {
            meta.removeStoredEnchant(toReplace);
            WbsEnchants.getInstance().getLogger().info("toReplace: " + toReplace.getKey());

            result.setItemMeta(meta);
            EnchantUtils.addEnchantment(definition, result, generateLevel());

            MerchantRecipe newRecipe = new MerchantRecipe(result,
                    recipe.getUses(),
                    recipe.getMaxUses(),
                    recipe.hasExperienceReward(),
                    recipe.getVillagerExperience(),
                    recipe.getPriceMultiplier(),
                    recipe.getDemand(),
                    recipe.getSpecialPrice());

            newRecipe.setIngredients(recipe.getIngredients());

            event.setRecipe(newRecipe);
        }
    }

    @Override
    public String toString() {
        return "VillagerTradeContext{" +
                "replaceableEnchants=" + replaceableEnchants +
                ", enchantment=" + definition +
                ", conditions=" + conditions +
                ", key='" + key + '\'' +
                '}';
    }
}
