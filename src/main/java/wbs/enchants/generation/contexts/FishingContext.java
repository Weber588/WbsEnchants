package wbs.enchants.generation.contexts;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EnchantUtils;

import java.util.List;

public class FishingContext extends ExistingLootContext {
    public static final String KEY = "fishing";

    public FishingContext(String key, WbsEnchantment enchantment, ConfigurationSection section, String directory) {
        super(key, enchantment, section, directory);
    }

    @Override
    protected int getDefaultChance() {
        int numOfCurses = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .getTag(EnchantmentTagKeys.CURSE)
                .size();

        return (int) (100.0 / (EnchantUtils.getAllEnchants().size() - numOfCurses));
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
