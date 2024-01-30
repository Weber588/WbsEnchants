package wbs.enchants.generation.contexts;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import wbs.enchants.WbsEnchantment;

public class MobDeathContext extends ExistingLootContext {
    public MobDeathContext(String key, WbsEnchantment enchantment, ConfigurationSection section, String directory) {
        super(key, enchantment, section, directory);
    }

    @Override
    protected int getDefaultChance() {
        return (int) enchantment.getRarity().getWeight();
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!shouldRun()) {
            return;
        }

        LivingEntity entity = event.getEntity();

        if (!meetsAllConditions(entity, entity.getLocation().getBlock(), entity.getLocation(), entity.getKiller())) {
            return;
        }

        tryAddingTo(event.getDrops());
    }
}
