package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import wbs.enchants.WbsEnchantment;

import java.util.Objects;

public class CastingEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "The hook is cast much further.";
    private static final double FORCE_MULTIPLIER = 1.5;

    public CastingEnchant() {
        super("casting", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.FISHING_ROD)
                .minimumCost(5, 6)
                .maximumCost(25, 6);
    }

    @EventHandler
    public void onCastRod(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.FISHING) {
            return;
        }

        ItemStack rod = event.getPlayer().getInventory().getItem(Objects.requireNonNull(event.getHand()));

        if (isEnchantmentOn(rod)) {
            int level = getLevel(rod);

            Vector velocity = event.getHook().getVelocity().multiply(Math.pow(FORCE_MULTIPLIER, level));
            event.getHook().setVelocity(velocity);
        }
    }
}
