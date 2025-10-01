package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import wbs.enchants.enchantment.helper.WbsCurse;

public class CurseInsomnia extends WbsCurse {
    private static final String DEFAULT_DESCRIPTION = "Prevents sleeping while worn.";

    public CurseInsomnia() {
        super("insomnia", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR);
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        // Just check if there's ANY armour with this enchant on it, don't care about details
        if (getHighestEnchantedArmour(player) != null) {
            event.setCancelled(true);
            sendActionBar(displayName().append(Component.text("prevents your sleep...")), player);
        }
    }
}
