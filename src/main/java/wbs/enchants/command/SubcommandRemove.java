package wbs.enchants.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.plugin.WbsPlugin;

public class SubcommandRemove extends EnchantmentSubcommand {
    public SubcommandRemove(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected void onEnchantCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, WbsEnchantment enchant) {
        
    }
}
