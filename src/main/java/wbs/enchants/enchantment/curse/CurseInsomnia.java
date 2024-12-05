package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;

public class CurseInsomnia extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "An armour curse that prevents the player from " +
            "sleeping while worn.";

    public CurseInsomnia() {
        super("curse/insomnia", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Insomnia";
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        // Just check if there's ANY armour with this enchant on it, don't care about details
        if (getHighestEnchantedArmour(player) != null) {
            event.setCancelled(true);
            sendActionBar("&c" + getDisplayName() + "&7 prevents your sleep...", player);
        }
    }

    @Override
    public EnchantmentType getType() {
        return EnchantmentTypeManager.CURSE;
    }
}
