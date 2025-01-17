package wbs.enchants.enchantment.helper;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.definition.EnchantmentExtension;

public interface TickableEnchant extends EnchantmentExtension {
    int getTickFrequency();

    /**
     * Called every {@link #getTickFrequency()} ticks, once per enchant
     */
    default void onGlobalTick() {

    }

    /**
     * Called every {@link #getTickFrequency()} ticks, once for each item stack enchanted with this enchantment
     * in the inventory of a {@link LivingEntity}
     * @param owner The entity that owns the inventory containing the itemStack
     * @param itemStack The itemStack that is enchanted with this enchantment
     * @param slot The slot in the owner's inventory that the itemStack exists in
     */
    default void onTickItemStack(LivingEntity owner, ItemStack itemStack, int slot) {

    }


    /**
     * Called every {@link #getTickFrequency()} ticks, once for each item stack enchanted with this enchantment
     * in an active item slot of a {@link LivingEntity}
     * @param owner The entity that owns the inventory containing the itemStack
     * @param itemStack The itemStack that is enchanted with this enchantment
     * @param slot The slot in the owner's inventory that the itemStack exists in
     */
    default void onTickEquipped(LivingEntity owner, ItemStack itemStack, EquipmentSlot slot) {

    }

    /**
     * Called every {@link #getTickFrequency()} ticks, once for each living entity that has an item in an active
     * equipment slot that has this enchantment
     * @param owner The entity that has an enchanted item in an active equipment slot.
     */
    default void onTickEquipped(LivingEntity owner) {

    }

    /**
     * Called every {@link #getTickFrequency()} ticks, once for each living entity that has an item in any inventory
     * slot that has this enchantment
     * @param owner The entity that has an enchanted item in an active equipment slot.
     */
    default void onTickAny(LivingEntity owner) {

    }
}
