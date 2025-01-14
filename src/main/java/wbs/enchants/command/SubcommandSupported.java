package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantManager;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandSupported extends WbsSubcommand {
    public SubcommandSupported(@NotNull WbsPlugin plugin) {
        super(plugin, "supported");

        permission = "wbsenchants.command." + label;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("&cThis command is only usable by players.", sender);
            return 0;
        }

        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.getType() == Material.AIR) {
            plugin.sendMessage("Hold an item to view enchantments applicable to it!", sender);
            return 0;
        }

        List<EnchantmentDefinition> enchants = new LinkedList<>(EnchantManager.getAllKnownDefinitions()
                .stream()
                .filter(enchantment -> enchantment.getEnchantment().canEnchantItem(held))
                .toList());

        if (enchants.isEmpty()) {
            plugin.sendMessage(WbsEnums.toPrettyString(held.getType()) + " does not support any enchantments.", sender);
        } else {
            enchants.sort(EnchantmentDefinition::compareTo);

            WbsMessageBuilder builder = plugin.buildMessage(
                    WbsEnums.toPrettyString(held.getType()) + " supports the following enchantment(s):\n"
            );

            EnchantmentDefinition first = enchants.getFirst();
            enchants.removeFirst();
            builder.append(first.interactiveDisplay());

            enchants.forEach(enchant -> {
                builder.append("&r, ");
                builder.append(enchant.interactiveDisplay());
            });

            builder.build().send(sender);
        }

        return Command.SINGLE_SUCCESS;
    }
}
