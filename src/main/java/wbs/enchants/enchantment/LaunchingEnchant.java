package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

public class LaunchingEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Gives you an upwards boost when you start gliding.";
    public LaunchingEnchant() {
        super("launching", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.ELYTRA)
                .maxLevel(2)
                .weight(1)
                .anvilCost(4);
    }

    @EventHandler
    public void onStartGliding(EntityToggleGlideEvent event) {
        if (!event.isGliding()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        ItemStack highestEnchanted = getHighestEnchanted(entity);
        if (highestEnchanted != null) {
            int level = getLevel(highestEnchanted);

            entity.setVelocity(entity.getVelocity().add(new Vector(0, (double) level / 1.5, 0)));
        }
    }
}
