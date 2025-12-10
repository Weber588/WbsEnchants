package wbs.enchants.enchantment.helper;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EventUtils;

import java.util.List;

public interface EntityEnchant extends AutoRegistrableEnchant, EnchantInterface {
    default void registerEntityEnchants() {
        EventUtils.register(EntityPlaceEvent.class, this::onPlace);
        EventUtils.register(EntityDeathEvent.class, this::onLivingEntityDeath);
    }

    @Nullable
    static Material getEntityMaterial(Entity entity) {
        return switch (entity) {
            case Boat boat -> boat.getBoatMaterial();
            case Minecart minecart -> minecart.getMinecartMaterial();
            case ArmorStand ignored -> Material.ARMOR_STAND;
            case GlowItemFrame ignored -> Material.GLOW_ITEM_FRAME;
            case ItemFrame ignored -> Material.ITEM_FRAME;
            case Painting ignored -> Material.PAINTING;
            default -> null;
        };
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

    default void onPlace(HangingPlaceEvent event) {

    }

    default void onPlace(EntityPlaceEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        Entity entity = event.getEntity();

        EntityEquipment equipment = player.getEquipment();
        ItemStack placedItem = equipment.getItem(event.getHand());

        if (!canEnchant(entity)) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        if (enchant.isEnchantmentOn(placedItem)) {
            int level = enchant.getLevel(placedItem);

            PersistentDataContainer container = entity.getPersistentDataContainer();
            container.set(key, PersistentDataType.INTEGER, level);

            afterPlace(new PlaceContext(player, entity, placedItem, level));
        }
    }

    default void onLivingEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Integer level = getLevel(entity);

        if (level != null) {
            List<ItemStack> drops = event.getDrops();

            Material material = EntityEnchant.getEntityMaterial(entity);
            if (material == null) {
                return;
            }

            for (ItemStack drop : drops) {
                if (drop.getType() == material) {
                    if (drop.getAmount() > 1) {
                        drop.subtract();

                        ItemStack enchanted = drop.asOne();

                        getThisEnchantment().tryAdd(enchanted, level);

                        drops.add(enchanted);
                        break;
                    }
                }
            }
        }
    }

    @OverrideOnly
    default void afterPlace(PlaceContext context) {

    }

    boolean canEnchant(Entity entity);

    record PlaceContext(Player player, Entity entity, ItemStack placedItem, int level) {

    }
}
