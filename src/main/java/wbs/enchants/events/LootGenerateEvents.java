package wbs.enchants.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import wbs.enchants.EnchantsSettings;

public class LootGenerateEvents implements Listener {

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        EnchantsSettings.getRegistered()
                .forEach(enchant -> enchant.onLootGenerate(event));
    }
}
