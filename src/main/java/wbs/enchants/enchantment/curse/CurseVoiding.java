package wbs.enchants.enchantment.curse;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;

public class CurseVoiding extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A bucket curse that causes any liquids picked up to simply " +
            "disappear into the void!";

    public CurseVoiding() {
        super("curse/voiding", DEFAULT_DESCRIPTION);

        supportedItems = WbsEnchantsBootstrap.BUCKET;
        maxLevel = 1;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Voiding";
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

    @Override
    public EnchantmentType getType() {
        return EnchantmentTypeManager.CURSE;
    }
}
