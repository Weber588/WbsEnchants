package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.enchantment.helper.WbsCurse;

public class CurseVoiding extends WbsCurse {
    private static final String DEFAULT_DESCRIPTION = "A bucket curse that causes any liquids picked up to be deleted.";

    public CurseVoiding() {
        super("voiding", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.BUCKET);
    }

    @EventHandler
    public void onFillBucket(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();

        ItemStack enchantedItem = getIfEnchanted(player, event.getHand());
        if (enchantedItem != null) {
            // Return original item, so it does... nothing
            event.setItemStack(enchantedItem);
        }
    }
}
