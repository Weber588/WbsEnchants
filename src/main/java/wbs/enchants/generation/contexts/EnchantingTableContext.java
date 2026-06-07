package wbs.enchants.generation.contexts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.events.enchanting.EnchantingContext;
import wbs.enchants.events.enchanting.GenerateTableEnchantsEvent;
import wbs.enchants.generation.GenerationContext;

import java.util.Collection;

public class EnchantingTableContext extends GenerationContext {
    public EnchantingTableContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);
    }

    @Override
    public void writeToSection(ConfigurationSection section) {

    }

    @Override
    protected int getDefaultChance() {
        return definition.weight() * 2 / 3;
    }

    @Override
    protected Component describeContext(TextComponent listBreak) {
        return Component.text("On enchanting table: " + chanceToRun() + "%");
    }

    @EventHandler
    public void onMobSpawn(GenerateTableEnchantsEvent event) {
        if (!shouldRun()) {
            return;
        }

        Collection<@NotNull Enchantment> availableOnTable = event.getAvailableOnTable();

        EnchantingContext context = event.getContext();

        Player enchanter = context.enchanter();

        if (!meetsAllConditions(enchanter, context.enchantingBlock(), context.enchantingBlock().getLocation(), enchanter)) {
            return;
        }

        availableOnTable.add(definition.getEnchantment());
    }
}
