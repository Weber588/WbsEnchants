package wbs.enchants.generation.contexts;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MobSpawnContext extends ExistingLootContext {
    public MobSpawnContext(String key, WbsEnchantment enchantment, ConfigurationSection section, String directory) {
        super(key, enchantment, section, directory);
    }

    @Override
    protected int getDefaultChance() {
        int rarityWeight = enchantment.getEnchantment().getWeight();
        return switch (Bukkit.getWorlds().getFirst().getDifficulty()) {
            case PEACEFUL -> 0;
            case EASY -> rarityWeight / 3;
            case NORMAL -> rarityWeight * 2 / 3;
            case HARD -> rarityWeight;
        };
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
