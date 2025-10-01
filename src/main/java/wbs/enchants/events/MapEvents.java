package wbs.enchants.events;

import io.papermc.paper.event.player.PlayerMapFilledEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class MapEvents implements Listener {
    @EventHandler
    public void onMapFill(PlayerMapFilledEvent event) {
        ItemStack createdMap = event.getCreatedMap();

        createdMap.addEnchantments(event.getOriginalItem().getEnchantments());
        event.setCreatedMap(createdMap);
    }
}
