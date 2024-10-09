package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.RomanNumerals;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandList extends Subcommand {
    public SubcommandList(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getArgument() {
        return Commands.literal(label).executes(context -> {
            CommandSender sender = context.getSource().getSender();

            List<WbsEnchantment> enchants = EnchantManager.getRegistered()
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());

            if (enchants.isEmpty()) {
                plugin.sendMessage("No enchantments enabled!", sender);
                return 0;
            }

            WbsMessageBuilder builder = new WbsMessageBuilder(plugin, "Enchantments: ");

            WbsEnchantment first = enchants.getFirst();
            enchants.removeFirst();
            appendEnchant(builder, first);

            enchants.forEach(enchant -> {
                builder.append("&r, ");
                appendEnchant(builder, enchant);
            });

            builder.build().send(sender);

            return Command.SINGLE_SUCCESS;
        });
    }

    private static String getKeyDisplay(WbsEnchantment enchantment) {
        String keyString = enchantment.getKey().getKey();

        if (EnchantUtils.isCurse(enchantment.getEnchantment())) {
            keyString = "&c" + keyString;
        } else {
            keyString = "&7" + keyString;
        }

        return keyString + "&r";
    }

    private static String getHoverText(WbsEnchantment enchant) {
        String text = "&h&m        &h " + enchant.getDefaultDisplayName() + "&h &m        ";

        text += "\n&rMax level: &h" + RomanNumerals.toRoman(enchant.getMaxLevel()) + " (" + enchant.getMaxLevel() + ")";
        text += "\n&rTarget: &h" + enchant.getTargetDescription();
        text += "\n&rDescription: &h" + enchant.getDescription();

        text += "\n\n&hClick to view full info!";

        return text;
    }

    private static void appendEnchant(WbsMessageBuilder builder, WbsEnchantment enchant) {
        builder.append(getKeyDisplay(enchant))
                .addHoverText(getHoverText(enchant))
                .addClickCommand("/" +
                        WbsEnchants.getInstance().getName().toLowerCase()
                                + ":customenchants info " + enchant.getKey().getKey()
                );
    }
}
