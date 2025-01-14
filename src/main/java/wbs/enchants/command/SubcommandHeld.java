package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantManager;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.RomanNumerals;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandHeld extends WbsSubcommand {
    private static final EnumSet<EnchantmentDefinition.DescribeOptions> DESCRIBE_OPTIONS = EnumSet.of(
            EnchantmentDefinition.DescribeOptions.TYPE,
            EnchantmentDefinition.DescribeOptions.DESCRIPTION,
            EnchantmentDefinition.DescribeOptions.MAX_LEVEL,
            EnchantmentDefinition.DescribeOptions.TARGET
    );

    public SubcommandHeld(@NotNull WbsPlugin plugin) {
        super(plugin, "held");

        permission = "wbsenchants.command." + label;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("&cThis command is only usable by players.", sender);
            return 0;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.isEmpty()) {
            plugin.sendMessage("Hold an item to view its enchants!.", sender);
            return Command.SINGLE_SUCCESS;
        }

        Map<Enchantment, Integer> enchantments = heldItem.getEnchantments();
        if (enchantments.isEmpty()) {
            plugin.sendMessage("Your held item does not have any enchantments!.", sender);
            return Command.SINGLE_SUCCESS;
        }

        List<Component> enchantComponents = new LinkedList<>();
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment ench = entry.getKey();
            Integer level = entry.getValue();

            EnchantmentDefinition def = EnchantManager.getFromKey(ench.key());

            if (def == null) {
                enchantComponents.add(ench.displayName(level).hoverEvent(HoverEvent.showText(EnchantUtils.getHoverText(ench))));
            } else {
                enchantComponents.add(def.interactiveDisplay(DESCRIBE_OPTIONS).append(Component.text(" " + RomanNumerals.toRoman(level))));
            }
        }
        Component lineBreak = Component.text("\n - ").color(plugin.getTextHighlightColour());

        Component enchantsMessage = Component.join(
                JoinConfiguration.builder().separator(
                        lineBreak
                ).parentStyle(
                        Style.style(plugin.getTextColour())
                ).build(),
                enchantComponents
        );

        plugin.buildMessage("Held item's enchants:\n")
                .append(lineBreak)
                .append(enchantsMessage)
                .send(sender);

        return Command.SINGLE_SUCCESS;
    }
}
