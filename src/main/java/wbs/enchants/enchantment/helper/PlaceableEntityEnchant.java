package wbs.enchants.enchantment.helper;

import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EventUtils;

public interface PlaceableEntityEnchant {
    default void registerPlaceEvents() {
        EventUtils.register(EntityPlaceEvent.class, this::onPlace);
        EventUtils.register(EntityDropItemEvent.class, this::onBreak);
    }

    default void onPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return;
        }

        Entity entity = event.getEntity();

        if (!canEnchant(entity)) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        ItemStack placedItem = equipment.getItem(event.getHand());
        if (enchant.containsEnchantment(placedItem)) {
            int level = enchant.getLevel(placedItem);

            PersistentDataContainer container = entity.getPersistentDataContainer();
            container.set(key, PersistentDataType.INTEGER, level);

            afterPlace(event);
        }
    }


    default void onBreak(EntityDropItemEvent event) {
        Entity entity = event.getEntity();
        PersistentDataContainer entityContainer = entity.getPersistentDataContainer();
        WbsEnchantment enchant = getThisEnchantment();

        ItemStack item = event.getItemDrop().getItemStack();
        if (!enchant.canEnchantItem(item)) {
            return;
        }

        NamespacedKey key = enchant.getKey();

        Integer level = entityContainer.get(key, PersistentDataType.INTEGER);
        if (level != null) {

            UberUtils.addEnchantment(enchant, item, level);
            afterDrop(event);
        }
    }

    default void afterPlace(EntityPlaceEvent event) {

    }

    default void afterDrop(EntityDropItemEvent event) {

    }

    WbsEnchantment getThisEnchantment();
    boolean canEnchant(Entity entity);
}
