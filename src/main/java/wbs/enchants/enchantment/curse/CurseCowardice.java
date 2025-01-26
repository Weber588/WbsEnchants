package wbs.enchants.enchantment.curse;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.enchantment.helper.WbsCurse;

public class CurseCowardice extends WbsCurse {
    private static final String DEFAULT_DESCRIPTION = "A curse that turns your weapon fearful, reducing its damage " +
            "the closer your enemies get.";

    public CurseCowardice() {
        super("cowardice",  DEFAULT_DESCRIPTION);
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
}
