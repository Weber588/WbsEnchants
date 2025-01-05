package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.VehicleEnchant;

public class ShulkenforcedEnchant extends WbsEnchantment implements VehicleEnchant {
    private static final String DEFAULT_DESCRIPTION = "Vehicles with this enchantment are imbued with the power of " +
            "shulkers, granting them resistance to gravity!";

    public ShulkenforcedEnchant() {
        super("shulkenforced", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_VEHICLE)
                .exclusiveInject(EnchantmentTagKeys.EXCLUSIVE_SET_MINING)
                .weight(10);
    }

    @Override
    public boolean canEnchant(Entity entity) {
        return entity instanceof Vehicle;
    }

    @Override
    public void afterPlace(EntityPlaceEvent event, ItemStack placedItem) {
        if (event.getEntity() instanceof Vehicle vehicle) {
            vehicle.setGravity(false);
        }
    }
}
