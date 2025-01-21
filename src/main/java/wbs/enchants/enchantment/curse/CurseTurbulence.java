package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.WbsMath;

public class CurseTurbulence extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "An elytra curse that causes turbulence while flying, " +
            "making it harder to stay on track!";

    private static final int CHANCE_PER_LEVEL_PER_TICK = 1;

    public CurseTurbulence() {
        super("curse/turbulence", EnchantmentTypeManager.CURSE, "Curse of Turbulence", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
                .supportedItems(ItemTypeKeys.ELYTRA);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }

        ItemStack enchantedItem = getIfEnchanted(player, EquipmentSlot.CHEST);
        if (enchantedItem != null && enchantedItem.getType() == Material.ELYTRA) {
            int level = getLevel(enchantedItem);
            if (WbsMath.chance(level * CHANCE_PER_LEVEL_PER_TICK)) {
                player.setVelocity(player.getVelocity().add(WbsMath.randomVector(level * 0.1)));
            }
        }
    }
}
