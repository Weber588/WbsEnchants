package wbs.enchants.generation.contexts;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.generation.GenerationContext;
import wbs.enchants.util.EnchantUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VillagerTradeContext extends GenerationContext {

    private final List<String> replaceableEnchants;

    public VillagerTradeContext(String key, WbsEnchantment enchantment, ConfigurationSection section, String directory) {
        super(key, enchantment, section, directory);

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
                .filter(ench -> replaceableEnchants.contains(ench.getKey().toString().toLowerCase()))
                .findAny()
                .orElse(null);

        if (toReplace != null) {
            storedEnchants.remove(toReplace);

            result.setItemMeta(meta);
            UberUtils.addStoredEnchantment(enchantment, result, generateLevel());

            event.setRecipe(recipe);
        }
    }
}