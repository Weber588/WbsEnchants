package wbs.enchants.enchantment;

import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsMath;

public class ReinforcedEnchant extends WbsEnchantment {
    private static final int PERCENT_TO_PREVENT_COOLDOWN = 25;

    private static final String DEFAULT_DESCRIPTION = "Gives a " + PERCENT_TO_PREVENT_COOLDOWN + "% chance to " +
            "prevent the shield from being disabled.";

    public ReinforcedEnchant() {
        super("reinforced", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.SHIELD);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlock(PlayerShieldDisableEvent event) {
        Player player = event.getPlayer();

        ItemStack enchanted = getIfEnchanted(player, EquipmentSlot.HAND);
        if (enchanted == null) {
            enchanted = getIfEnchanted(player, EquipmentSlot.OFF_HAND);
        }

        if (enchanted != null) {
            int level = getLevel(enchanted);
            if (WbsMath.chance(PERCENT_TO_PREVENT_COOLDOWN * level)) {
                event.setCancelled(true);
            }
        }
    }
}
