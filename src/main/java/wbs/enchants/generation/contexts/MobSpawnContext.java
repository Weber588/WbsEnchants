package wbs.enchants.generation.contexts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.definition.EnchantmentDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MobSpawnContext extends ExistingLootContext {
    public MobSpawnContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);
    }

    @Override
    protected int getDefaultChance() {
        return definition.weight() * 2 / 3;
    }

    @Override
    protected Component describeContext(TextComponent listBreak) {
        return Component.text("On spawned mob's equipment: " + chanceToRun() + "%");
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!shouldRun()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!meetsAllConditions(entity, null)) {
            return;
        }

        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return;
        }

        List<ItemStack> equipmentItems = Arrays.stream(EquipmentSlot.values())
                .map(equipment::getItem)
                .collect(Collectors.toList());

        tryAddingTo(equipmentItems);
    }
}
