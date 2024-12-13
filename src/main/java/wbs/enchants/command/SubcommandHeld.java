package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandHeld extends EnchantmentSubcommand {
    public SubcommandHeld(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return execute(context);
    }

    @Override
    protected int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (sender instanceof Player player) {
            ItemStack held = player.getInventory().getItemInMainHand();

            if (held.getType() == Material.AIR) {
                plugin.sendMessage("Hold an item to view enchantments applicable to it!", sender);
                return 0;
            }

            List<Enchantment> enchants = new LinkedList<>(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)
                    .stream()
                    .filter(enchantment -> enchantment.canEnchantItem(held))
                    .toList());

            if (enchants.isEmpty()) {
                plugin.sendMessage(WbsEnums.toPrettyString(held.getType()) + " does not support any enchantments.", sender);
            } else {
                enchants.sort(Comparator.comparing(Keyed::getKey));

                WbsMessageBuilder builder = plugin.buildMessage(
                        WbsEnums.toPrettyString(held.getType()) + " supports the following enchantment(s):\n"
                );

                Enchantment first = enchants.getFirst();
                enchants.removeFirst();
                appendEnchant(builder, first);

                enchants.forEach(enchant -> {
                    builder.append("&r, ");
                    appendEnchant(builder, enchant);
                });

                builder.build().send(sender);
            }

            return Command.SINGLE_SUCCESS;
        } else {
            plugin.sendMessage("&cThis command is only usable by players.", sender);
            return 0;
        }
    }


    private static void appendEnchant(WbsMessageBuilder builder, Enchantment enchant) {
        boolean isCustom = EnchantUtils.isWbsManaged(enchant);

        Component hoverText = EnchantUtils.getHoverText(enchant);
        if (isCustom) {
            hoverText = hoverText.append(Component.text("\n\nClick to view full info!"))
                    .color(WbsEnchants.getInstance().getTextColour());
        }

        builder.append(EnchantUtils.getDisplayName(enchant))
                .addHoverText(hoverText);

        if (isCustom) {
            builder.addClickCommand("/" +
                    WbsEnchants.getInstance().getName().toLowerCase()
                    + ":customenchants info " + enchant.getKey().getKey()
            );
        }
    }
}
