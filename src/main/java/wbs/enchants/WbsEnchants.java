package wbs.enchants;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.command.*;
import wbs.enchants.events.LeashEvents;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
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

        List<Subcommand> subcommands = List.of(
                new SubcommandInfo(this, "info"),
                new SubcommandList(this, "list"),
                new SubcommandHeld(this, "held"),
                new SubcommandAdd(this, "add")
        );

        LifecycleEventManager<@NotNull Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("customenchantments");

            String options = subcommands.stream()
                    .map(Subcommand::getLabel)
                    .collect(Collectors.joining("|"));

            builder.executes(context -> {
                sendMessage(
                        "Usage: &h/" + context.getInput().split(" ")[0] + " [" + options + "]",
                        context.getSource().getSender());
                return Command.SINGLE_SUCCESS;
            });

            for (Subcommand subcommand : subcommands) {
                builder.then(subcommand.getArgument());
            }

            event.registrar().register(builder.build(),
                    "Commands relating to the WbsEnchantments plugin.",
                    List.of(
                            "wbsenchants:customenchantments",
                            "cench",
                            "wbsenchants:cench",
                            "customenchants",
                            "wbsenchants:customenchants"
                    )
            );
        });

        registerListener(new LeashEvents());

        settings.reload();
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
