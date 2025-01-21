package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.type.EnchantmentTypeManager;

public class CurseVoiding extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A bucket curse that causes any liquids picked up to simply " +
            "disappear into the void!";

    public CurseVoiding() {
        super("curse/voiding", EnchantmentTypeManager.CURSE, "Curse of Voiding", DEFAULT_DESCRIPTION);

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
