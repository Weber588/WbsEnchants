package wbs.enchants.generation.contexts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PiglinBarterEvent;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.utils.util.entities.selector.RadiusSelector;

public class BarterContext extends ExistingLootContext {
    public BarterContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);
    }

    @Override
    protected int getDefaultChance() {
        return definition.getEnchantment().getWeight();
    }

    @Override
    protected Component describeContext(TextComponent listBreak) {
        return Component.text("Piglin bartering: " + chanceToRun() + "%");
    }

    @EventHandler
    public void onBarter(PiglinBarterEvent event) {
        if (!shouldRun()) {
            return;
        }

        Piglin piglin = event.getEntity();
        Player player = new RadiusSelector<>(Player.class).setRange(50).selectFirst(piglin);

        if (!meetsAllConditions(piglin, piglin.getLocation().getBlock(), piglin.getLocation(), player)) {
            return;
        }

        tryAddingTo(event.getOutcome());
    }
}
