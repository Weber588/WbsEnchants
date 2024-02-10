package wbs.enchants.generation.contexts;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import wbs.enchants.WbsEnchantment;

import java.util.LinkedList;
import java.util.List;

public class LootTableContext extends ExistingLootContext {

    private List<String> tables = new LinkedList<>();

    public LootTableContext(String key, WbsEnchantment enchantment, ConfigurationSection section, String directory) {
        super(key, enchantment, section, directory);

        if (section.isList("tables")) {
            tables = section.getStringList("tables");
        }
    }

    @Override
    protected int getDefaultChance() {
        return (int) enchantment.getRarity().getWeight() * 3;
    }

    private boolean canAddTo(LootTable table) {
        if (tables.isEmpty()) {
            return true;
        }

        String tableKey = table.getKey().toString();
        String nameKey = table.getKey().getKey();
        for (String tableName : tables) {
            if (tableKey.matches(tableName)) {
                return true;
            }
            if (nameKey.matches(tableName)) {
                return true;
            }
        }
        return false;
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

        if (!canAddTo(event.getLootTable())) {
            return;
        }

        tryAddingTo(event.getLoot());
    }
}
