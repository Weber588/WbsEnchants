package wbs.enchants.enchantment.helper;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.apache.commons.lang3.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.definition.EnchantmentExtension;
import wbs.enchants.util.EventUtils;

import java.util.List;
import java.util.Map;

public interface ItemModificationEnchant extends EnchantmentExtension, EnchantInterface, AutoRegistrableEnchant {
    NamespacedKey HAS_ENCHANT_MODIFICATION_KEY = WbsEnchantsBootstrap.createKey("has_enchant_modification");

    default void registerModificationEvents() {
        EventUtils.register(EnchantItemEvent.class, this::onEnchant);
        EventUtils.register(PrepareResultEvent.class, this::onGrindstone);
        EventUtils.register(LootGenerateEvent.class, this::onLootGenerate);
        EventUtils.register(EntitySpawnEvent.class, this::onMobSpawn);
    }

    default void onEnchant(EnchantItemEvent event) {
        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();

        if (enchantsToAdd.containsKey(getThisEnchantment().getEnchantment())) {
            ItemStack item = event.getItem();

            applyModifications(item);

            event.setItem(item);
        }
    }

    default void onGrindstone(PrepareResultEvent event) {
        if (event.getView().getType() != InventoryType.GRINDSTONE) {
            return;
        }
        if (event.getResult() == null) {
            return;
        }

        ItemStack result = event.getResult();
        validateUpdateItem(result);
        event.setResult(result);
    }

    default void onLootGenerate(LootGenerateEvent event) {
        List<ItemStack> items = event.getLoot();

        for (ItemStack item : items) {
            validateUpdateItem(item);
        }

        event.setLoot(items);
    }

    default void onMobSpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) {
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = equipment.getItem(slot);
            if (!item.isEmpty()) {
                validateUpdateItem(item);
            }
        }
    }

    default boolean isAppliedTo(ItemStack item) {
        PersistentDataContainerView container = item.getPersistentDataContainer();

        if (!container.has(HAS_ENCHANT_MODIFICATION_KEY)) {
            return false;
        }

        PersistentDataContainer modifications = container.get(HAS_ENCHANT_MODIFICATION_KEY, PersistentDataType.TAG_CONTAINER);
        if (modifications == null || modifications.isEmpty()) {
            return false;
        }

        return modifications.has(getThisEnchantment().getKey());
    }

    default boolean validateUpdateItem(ItemStack item) {
        WbsEnchantment ench = getThisEnchantment();

        boolean isAppliedTo = isAppliedTo(item);
        boolean isEnchanted = ench.isEnchantmentOn(item);

        boolean wasValid = true;
        if (isAppliedTo && !isEnchanted) {
            wasValid = false;

            removeModifications(item);
        } else if (!isAppliedTo && isEnchanted) {
            wasValid = false;

            applyModifications(item);
        }

        return wasValid;
    }

    default void applyModifications(ItemStack item) {
        PersistentDataContainerView container = item.getPersistentDataContainer();
        PersistentDataContainer modifications = container.get(HAS_ENCHANT_MODIFICATION_KEY, PersistentDataType.TAG_CONTAINER);
        Validate.notNull(modifications);

        modifyItem(item);

        modifications.set(getThisEnchantment().getKey(), PersistentDataType.BOOLEAN, true);
    }

    default void removeModifications(ItemStack item) {
        PersistentDataContainerView container = item.getPersistentDataContainer();
        PersistentDataContainer modifications = container.get(HAS_ENCHANT_MODIFICATION_KEY, PersistentDataType.TAG_CONTAINER);
        Validate.notNull(modifications);

        unmodifyItem(item);

        modifications.remove(getThisEnchantment().getKey());
    }

    void modifyItem(ItemStack item);
    void unmodifyItem(ItemStack item);
}
