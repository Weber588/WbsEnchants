package wbs.enchants;

import org.bukkit.event.HandlerList;
import wbs.enchants.command.*;
import wbs.enchants.events.LeashEvents;
import wbs.enchants.events.LootGenerationBlocker;
import wbs.utils.util.commands.brigadier.WbsCommand;
import wbs.utils.util.commands.brigadier.WbsErrorsSubcommand;
import wbs.utils.util.commands.brigadier.WbsReloadSubcommand;
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

        WbsCommand.getStatic(this,
                "customenchantments",
                "Commands relating to the WbsEnchantments plugin."
        ).addAliases(
                "cench",
                "customenchants"
        ).addSubcommands(
                new SubcommandInfo(this),
                new SubcommandList(this),
                new SubcommandHeld(this),
                new SubcommandAdd(this),
                new SubcommandTagInfo(this),
                WbsReloadSubcommand.getStatic(this, settings),
                WbsErrorsSubcommand.getStatic(this, settings)
        ).register();

        registerListener(new LeashEvents());
        registerListener(new LootGenerationBlocker());

        settings.reload();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
