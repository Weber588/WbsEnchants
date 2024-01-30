package wbs.enchants.generation.contexts;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.entities.selector.RadiusSelector;

import java.util.List;

public class FishingContext extends ExistingLootContext {
    public static final String KEY = "fishing";

    public FishingContext(String key, WbsEnchantment enchantment, ConfigurationSection section, String directory) {
        super(key, enchantment, section, directory);
    }

    @Override
    protected int getDefaultChance() {
        return (int) (100.0 / (EnchantUtils.getAllEnchants().size() - 2));
    }

    @EventHandler
    public void onFishEvent(PlayerFishEvent event) {
        if (!shouldRun()) {
            return;
        }

        if (!(event.getCaught() instanceof Item item)) {
            return;
        }

        Player player = event.getPlayer();

        if (!meetsAllConditions(item, item.getLocation().getBlock(), item.getLocation(), player)) {
            return;
        }

        tryAddingTo(List.of(item.getItemStack()));
    }
}
