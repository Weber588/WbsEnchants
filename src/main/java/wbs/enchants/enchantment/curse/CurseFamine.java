package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;

public class CurseFamine extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A curse that has a chance to turn food from slain mobs rotten.";

    public CurseFamine() {
        super("curse/famine", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.ENCHANTABLE_WEAPON;
        maxLevel = 1;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Cowardice";
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        ItemStack enchantedItem = getIfEnchanted(attacker);
        if (enchantedItem != null) {

            AttributeInstance reachAttribute = attacker.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE);
            if (reachAttribute != null) {
                double reach = reachAttribute.getValue();

                double distanceToEntity = event.getEntity().getLocation().distance(attacker.getLocation());
                double modifier = distanceToEntity / reach;

                event.setDamage(event.getDamage() * modifier);
            }
        }
    }

    @Override
    public EnchantmentType getType() {
        return EnchantmentTypeManager.CURSE;
    }
}
