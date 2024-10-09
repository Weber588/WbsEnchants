package wbs.enchants.events;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import wbs.utils.util.persistent.WbsPersistentDataType;

public class LeashEvents implements Listener {
    public static final NamespacedKey LEASH_ITEM_KEY = new NamespacedKey("wbsenchants", "leash_item");

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeash(PlayerLeashEntityEvent event) {
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();

        ItemStack item = equipment.getItem(event.getHand());

        Entity entity = event.getEntity();

        ItemStack clone = item.clone();
        clone.setAmount(1);

        PersistentDataContainer container = entity.getPersistentDataContainer();

        container.set(LEASH_ITEM_KEY, WbsPersistentDataType.ITEM, clone);
    }

    @EventHandler
    public void onUnleash(EntityUnleashEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!entity.isLeashed()) {
            return;
        }

        PersistentDataContainer container = entity.getPersistentDataContainer();

        if (container.has(LEASH_ITEM_KEY, WbsPersistentDataType.ITEM)) {
            ItemStack item = container.get(LEASH_ITEM_KEY, WbsPersistentDataType.ITEM);
            if (item != null) {
                entity.getWorld().dropItemNaturally(entity.getEyeLocation(), item);
                entity.setLeashHolder(null);
            }
        }
    }
}
