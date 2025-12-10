package wbs.enchants.enchantment;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
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
                .weight(10);
    }

    @Override
    public boolean canEnchant(Entity entity) {
        return entity instanceof Vehicle;
    }

    @Override
    public void afterPlace(PlaceContext context) {
        if (context.entity() instanceof Vehicle vehicle) {
            vehicle.setGravity(false);
        }
    }
}
