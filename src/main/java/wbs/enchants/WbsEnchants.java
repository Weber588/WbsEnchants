package wbs.enchants;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import wbs.enchants.events.GrindstoneEvents;
import wbs.enchants.events.LootGenerateEvents;
import wbs.utils.util.plugin.WbsPlugin;

public class WbsEnchants extends WbsPlugin {
    private static WbsEnchants instance;
    public static WbsEnchants getInstance() {
        return instance;
    }

    public EnchantsSettings settings;

    @Override
    public void onEnable() {
        instance  = this;
        settings = new EnchantsSettings(this);

        registerListener(new GrindstoneEvents());
        registerListener(new LootGenerateEvents());

        settings.reload();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
