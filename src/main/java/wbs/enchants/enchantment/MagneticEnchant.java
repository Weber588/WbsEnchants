package wbs.enchants.enchantment;

import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.utils.util.entities.selector.RadiusSelector;

import java.util.HashMap;

public class MagneticEnchant extends WbsEnchantment implements TickableEnchant {
    private static final String DEFAULT_DESCRIPTION = "Your item pickup radius is extended.";

    public MagneticEnchant() {
        super("magnetic", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(4)
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_MAGNETIC_ARMOR)
                .activeSlots(EquipmentSlotGroup.ARMOR);
    }

    @Override
    public int getTickFrequency() {
        return 5;
    }

    private static final RadiusSelector<Item> SELECTOR = new RadiusSelector<>(Item.class)
            .setPredicate(item -> item.canPlayerPickup() && item.getPickupDelay() == 0);

    @Override
    public void onTickEquipped(LivingEntity owner, ItemStack itemStack, EquipmentSlot slot) {
        if (!owner.getCanPickupItems()) {
            return;
        }

        if (!(owner instanceof Player player)) {
            return;
        }

        int level = getLevel(itemStack);

        SELECTOR.setRange(level + 1)
                .select(owner)
                .forEach(item -> {
                    if (!item.isValid()) {
                        return;
                    }

                    PlayerInventory inventory = player.getInventory();
                    ItemStack stack = item.getItemStack();
                    HashMap<Integer, ItemStack> failed = inventory.addItem(stack);

                    int failedAmount = 0;
                    for (ItemStack failedItem : failed.values()) {
                        failedAmount += failedItem.getAmount();
                    }

                    player.playPickupItemAnimation(item, stack.getAmount() - failedAmount);

                    if (failedAmount == 0) {
                        item.remove();
                    } else {
                        stack.setAmount(stack.getAmount() - failedAmount);

                        item.setItemStack(stack);
                    }
                });
    }
}
