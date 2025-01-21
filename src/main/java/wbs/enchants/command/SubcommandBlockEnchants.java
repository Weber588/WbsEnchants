package wbs.enchants.command;

import com.mojang.brigadier.Command;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.WbsStringify;

import java.util.Map;

public class SubcommandBlockEnchants extends AbstractBlockSubcommand {
    public SubcommandBlockEnchants(@NotNull WbsPlugin plugin) {
        super(plugin, "enchants");
    }

    @Override
    public int executeOnBlock(CommandSender sender, Block block) {
        Map<BlockEnchant, Integer> blockEnchantments = BlockEnchant.getEnchantments(block);

        String blockAsString = WbsStringify.toString(block.getLocation(), !(sender instanceof Player));

        if (blockEnchantments.isEmpty()) {
            plugin.sendMessage("There are no enchantments on " + blockAsString, sender);
            return Command.SINGLE_SUCCESS;
        }

        WbsMessageBuilder builder = plugin.buildMessage("Enchantments on " + blockAsString + ":");
        blockEnchantments.forEach((enchant, level) -> {
            builder.append("\n  - ")
                    .append(enchant.getThisEnchantment().getDefinition().interactiveDisplay(level));
        });

        builder.send(sender);

        return Command.SINGLE_SUCCESS;
    }
}
