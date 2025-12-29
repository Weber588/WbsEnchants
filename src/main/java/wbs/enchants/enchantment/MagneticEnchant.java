package wbs.enchants.enchantment;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.utils.util.entities.selector.RadiusSelector;

import java.util.HashMap;

public class MagneticEnchant extends WbsEnchantment implements TickableEnchant {
    private static final String DEFAULT_DESCRIPTION = "Your item pickup radius is extended.";
    public static final int DEFAULT_PICKUP_RADIUS = 1;

    public MagneticEnchant() {
        super("magnetic", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(4)
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_MAGNETIC);
    }

    private double radiusPerLevel = 1;

    @Override
    public void configure(@NotNull ConfigurationSection section, String directory) {
        super.configure(section, directory);

        radiusPerLevel = section.getDouble("radius-per-level", radiusPerLevel);
    }

    @Override
    public int getTickFrequency() {
        return 5;
    }

    private static final RadiusSelector<Item> SELECTOR = new RadiusSelector<>(Item.class)
            .setPredicate(item -> item.canPlayerPickup() && item.getPickupDelay() == 0);

    @Override
    public void onTickEquipped(LivingEntity owner) {
        if (!owner.getCanPickupItems()) {
            return;
        }

        if (!(owner instanceof Player player)) {
            return;
        }

        ItemStack highestEnchanted = getHighestEnchanted(owner);
        int level = getLevel(highestEnchanted);

        SELECTOR.setRange((level * radiusPerLevel) + DEFAULT_PICKUP_RADIUS)
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

                    int pickedUp = stack.getAmount() - failedAmount;

                    if (pickedUp > 0) {
                        player.playPickupItemAnimation(item, pickedUp);
                    }

                    if (failedAmount == 0) {
                        item.remove();
                    } else {
                        stack.setAmount(failedAmount);

                        item.setItemStack(stack);
                    }
                });
    }
}
