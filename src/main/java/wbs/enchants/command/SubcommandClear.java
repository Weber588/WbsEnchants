package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.utils.util.plugin.WbsPlugin;

public class SubcommandClear extends EnchantmentSubcommand {
    public SubcommandClear(@NotNull WbsPlugin plugin) {
        super(plugin, "clear");
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return clear(context, null, true);
    }

    @Override
    protected boolean filter(@Nullable CommandContext<?> context, EnchantmentDefinition definition) {
        if (context == null) {
            return true;
        }

        if (context.getSource() instanceof CommandSourceStack source) {
            if (source.getSender() instanceof Player player) {
                return player.getInventory().getItemInMainHand().getEnchantmentLevel(definition.getEnchantment()) > 0;
            }
        }

        return true;
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> context) {
        return clear(context, getEnchantment(context), false);
    }

    @Override
    protected int executeLevel(CommandContext<CommandSourceStack> context) {
        return clear(context, getEnchantment(context), false);
    }

    private int clear(CommandContext<CommandSourceStack> context, EnchantmentDefinition definition, boolean clearAll) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            plugin.sendMessage("&wThis command is only usable by players.", context.getSource().getSender());
            return 0;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.isEmpty()) {
            plugin.sendMessage("&wHold an item to clear enchants from.", context.getSource().getSender());
            return 0;
        }

        if (clearAll) {
            heldItem.removeEnchantments();
        } else {
            if (definition == null) {
                plugin.sendMessage("Enchantment not found for key &h\"" + getEnchantmentKey(context) + "\".", sender);
                return Command.SINGLE_SUCCESS;
            }

            heldItem.removeEnchantment(definition.getEnchantment());
        }

        return Command.SINGLE_SUCCESS;
    }
}
