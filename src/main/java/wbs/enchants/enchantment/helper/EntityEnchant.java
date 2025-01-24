package wbs.enchants.enchantment.helper;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EventUtils;

public interface EntityEnchant extends AutoRegistrableEnchant, EnchantInterface {
    default void registerEntityEnchants() {
        EventUtils.register(EntityPlaceEvent.class, this::onPlace);
    }

    default boolean isEnchanted(Entity entity) {
        return getLevel(entity) != null;
    }

    @Nullable
    default Integer getLevel(Entity entity) {
        PersistentDataContainer entityContainer = entity.getPersistentDataContainer();
        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        return entityContainer.get(key, PersistentDataType.INTEGER);
    }

    default void onPlace(EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Vehicle)) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        EntityEquipment equipment = player.getEquipment();

        Entity entity = event.getEntity();

        if (!canEnchant(entity)) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        ItemStack placedItem = equipment.getItem(event.getHand());
        if (enchant.isEnchantmentOn(placedItem)) {
            int level = enchant.getLevel(placedItem);

            PersistentDataContainer container = entity.getPersistentDataContainer();
            container.set(key, PersistentDataType.INTEGER, level);

            afterPlace(event, placedItem);
        }
    }

    default void afterPlace(EntityPlaceEvent event, ItemStack placedItem) {

    }

    boolean canEnchant(Entity entity);
}
