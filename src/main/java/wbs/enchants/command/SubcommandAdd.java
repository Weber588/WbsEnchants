package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.plugin.WbsPlugin;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandAdd extends EnchantmentSubcommand {
    public SubcommandAdd(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        plugin.sendMessage("Usage: &h/cench add <enchantment>", sender);
        return 0;
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> context) {
        WbsEnchantment enchant = context.getArgument("enchantment", WbsEnchantment.class);
        return enchant(context, enchant, 1);
    }

    @Override
    protected int executeLevel(CommandContext<CommandSourceStack> context) {
        WbsEnchantment enchant = context.getArgument("enchantment", WbsEnchantment.class);
        int level = context.getArgument("level", Integer.class);

        return enchant(context, enchant, level);
    }

    private int enchant(CommandContext<CommandSourceStack> context, WbsEnchantment enchant, int level) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("&wThis command is only usable by players.", context.getSource().getSender());
            return 0;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.isEmpty()) {
            plugin.sendMessage("&wHold an item to enchant!", context.getSource().getSender());
            return 0;
        }

        heldItem.addUnsafeEnchantment(enchant.getEnchantment(), level);

        return Command.SINGLE_SUCCESS;
    }
}
