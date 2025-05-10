package wbs.enchants.enchantment;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.VehicleEnchant;

public class FrictionlessEnchant extends WbsEnchantment implements VehicleEnchant {
    private static final String DEFAULT_DESCRIPTION = "A minecart enchantment that increases its maximum speed, " +
            "but does not help it get there.";

    public FrictionlessEnchant() {
        super("frictionless", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.MINECARTS)
                .maxLevel(3)
                .targetDescription("Minecart");
    }

    @Override
    public void afterPlace(EntityPlaceEvent event, ItemStack placedItem) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Minecart minecart)) {
            return;
        }

        int level = getLevel(placedItem);

        double defaultMax = minecart.getMaxSpeed();

        minecart.setMaxSpeed(defaultMax * (1 + level / 3.0));
    }

    @Override
    public boolean canEnchant(Entity entity) {
        return entity instanceof Minecart;
    }
}
