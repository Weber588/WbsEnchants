package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.enchantment.helper.WbsCurse;

public class CurseMercy extends WbsCurse {
    private static final String DEFAULT_DESCRIPTION = "A weapon curse that refuses to deal a killing blow.";

    public CurseMercy() {
        super("mercy",  DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Player killer)) {
            return;
        }

        ItemStack enchantedItem = getIfEnchanted(killer);
        if (enchantedItem != null) {
            event.setCancelled(true);
            event.setReviveHealth(2);
            sendActionBar("Your weapon refuses to deal the killing blow...", killer);
        }
    }
}
