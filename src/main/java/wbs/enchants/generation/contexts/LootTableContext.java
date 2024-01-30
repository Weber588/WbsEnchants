package wbs.enchants.generation.contexts;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.loot.LootContext;
import wbs.enchants.WbsEnchantment;

public class LootTableContext extends ExistingLootContext {
    public LootTableContext(String key, WbsEnchantment enchantment, ConfigurationSection section, String directory) {
        super(key, enchantment, section, directory);
    }

    @Override
    protected int getDefaultChance() {
        return (int) enchantment.getRarity().getWeight() * 3;
    }

    @EventHandler
    public void onLootTableGenerate(LootGenerateEvent event) {
        if (!shouldRun()) {
            return;
        }

        LootContext context = event.getLootContext();
        Player player = null;
        if (context.getKiller() instanceof Player) {
            player = (Player) context.getKiller();
        } else if (event.getEntity() instanceof Player) {
            player = (Player) event.getEntity();
        }

        if (!meetsAllConditions(context.getLootedEntity(), context.getLocation().getBlock(), context.getLocation(), player)) {
            return;
        }

        tryAddingTo(event.getLoot());
    }
}
