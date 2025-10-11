package wbs.enchants.generation.contexts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import wbs.enchants.definition.EnchantmentDefinition;

public class MobDeathContext extends ExistingLootContext {
    public MobDeathContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);
    }

    @Override
    protected int getDefaultChance() {
        return definition.weight();
    }

    @Override
    protected Component describeContext(TextComponent listBreak) {
        return Component.text("On mob death: " + chanceToRun() + "%");
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
