package wbs.enchants.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.plugin.WbsPlugin;

public abstract class Subcommand {
    protected final WbsPlugin plugin;
    protected final String label;

    public Subcommand(@NotNull WbsPlugin plugin, @NotNull String label) {
        this.plugin = plugin;
        this.label = label;
    }

    public abstract LiteralArgumentBuilder<CommandSourceStack> getArgument();

    public String getLabel() {
        return label;
    }
}
