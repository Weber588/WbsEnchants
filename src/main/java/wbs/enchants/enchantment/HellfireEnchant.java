package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;

public class HellfireEnchant extends WbsEnchantment {
    private static final String DESCRIPTION = "Makes this item produce soul fire instead of regular fire!";

    public HellfireEnchant() {
        super("hellfire", DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.FLINT_AND_STEEL);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        Entity igniter = event.getIgnitingEntity();
        if (!(igniter instanceof Player player)) {
            return;
        }

        ItemStack item = getIfEnchanted(player, EquipmentSlot.HAND);
        if (item == null) {
            item = getIfEnchanted(player, EquipmentSlot.OFF_HAND);
            if (item == null) {
                return;
            }
        }

        event.getBlock().setType(Material.SOUL_FIRE, false);
    }
}
