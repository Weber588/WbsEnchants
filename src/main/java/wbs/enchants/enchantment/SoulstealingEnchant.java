package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsMath;

import java.util.Set;

public class SoulstealingEnchant extends WbsEnchantment {
    private static final double CHANCE_PER_LEVEL = 0.1;
    private static final String DEFAULT_DESCRIPTION = "Adds a " + CHANCE_PER_LEVEL + "% chance per level of killed mobs dropping its spawn egg!";

    // TODO: Make configurable
    private final Set<Material> blacklistedMaterials = Set.of(
            Material.ELDER_GUARDIAN_SPAWN_EGG,
            Material.ENDER_DRAGON_SPAWN_EGG,
            Material.WITHER_SPAWN_EGG,
            Material.IRON_GOLEM_SPAWN_EGG,
            Material.SNOW_GOLEM_SPAWN_EGG,
            Material.WARDEN_SPAWN_EGG
    );

    public SoulstealingEnchant() {
        super("soulstealing", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON)
                .maxLevel(3);

    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        LivingEntity killer = entity.getKiller();
        if (killer == null) {
            return;
        }

        ItemStack item = getIfEnchanted(killer);

        if (item != null) {
            int level = getLevel(item);
            if (WbsMath.chance(CHANCE_PER_LEVEL * level)) {
                Material spawnEggType = getSpawnEggType(entity);

                if (spawnEggType != null) {
                    event.getDrops().add(ItemStack.of(spawnEggType, 1));
                }
            }
        }
    }

    private Material getSpawnEggType(LivingEntity entity) {
        EntityType type = entity.getType();

        for (Material material : Material.values()) {
            if (blacklistedMaterials.contains(material)) {
                continue;
            }
            if (material.name().equals(type.name() + "_SPAWN_EGG")) {
                return material;
            }
        }

        return null;
    }
}
