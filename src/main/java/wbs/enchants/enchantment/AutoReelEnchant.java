package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.FishingEnchant;

public class AutoReelEnchant extends WbsEnchantment implements FishingEnchant {
    private static final String DEFAULT_DESCRIPTION = "Automatically reels in the fishing rod for you!";

    public AutoReelEnchant() {
        super("auto_reel", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.FISHING_ROD)
                .minimumCost(10, 10)
                .maximumCost(55, 10);
    }

    @Override
    public void onFishEvent(PlayerFishEvent event, ItemStack rod, EquipmentSlot hand) {
        if (event.getState() != PlayerFishEvent.State.BITE) {
            return;
        }

        // Run next tick so it's actually considered a successful pull
        WbsEnchants.getInstance().runSync(() ->
                reelIn(event.getPlayer(), event.getHook(), rod, hand)
        );
    }
}
