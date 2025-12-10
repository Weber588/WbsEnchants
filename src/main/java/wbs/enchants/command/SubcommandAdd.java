package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.utils.util.plugin.WbsPlugin;

public class SubcommandAdd extends EnchantmentSubcommand {
    public SubcommandAdd(@NotNull WbsPlugin plugin) {
        super(plugin, "add");
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        plugin.sendMessage("Usage: &h/cench add <enchantment>", sender);
        return 0;
    }

    @Override
    protected boolean filter(@Nullable CommandContext<?> context, EnchantmentDefinition definition) {
        if (context == null) {
            return true;
        }

        if (context.getSource() instanceof CommandSourceStack source) {
            if (source.getSender() instanceof Player player) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.BOOK) {
                    return true;
                }
                return definition.getEnchantment().canEnchantItem(item);
            }
        }

        return true;
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> context) {
        return enchant(context, getEnchantment(context), 1);
    }

    @Override
    protected int executeLevel(CommandContext<CommandSourceStack> context) {
        int level = context.getArgument("level", Integer.class);

        return enchant(context, getEnchantment(context), level);
    }

    private int enchant(CommandContext<CommandSourceStack> context, EnchantmentDefinition definition, int level) {
        CommandSender sender = context.getSource().getSender();
        if (definition == null) {
            plugin.sendMessage("Enchantment not found for key &h\"" + getEnchantmentKey(context) + "\".", sender);
            return Command.SINGLE_SUCCESS;
        }

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("&wThis command is only usable by players.", context.getSource().getSender());
            return 0;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.isEmpty()) {
            plugin.sendMessage("&wHold an item to enchant!", context.getSource().getSender());
            return 0;
        }

        if (heldItem.getType() == Material.BOOK) {
            heldItem.setType(Material.ENCHANTED_BOOK);
        }

        heldItem.addUnsafeEnchantment(definition.getEnchantment(), level);

        return Command.SINGLE_SUCCESS;
    }
}
