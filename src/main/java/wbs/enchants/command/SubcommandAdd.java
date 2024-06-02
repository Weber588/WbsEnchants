package wbs.enchants.command;

import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SubcommandAdd extends EnchantmentSubcommand {
    public SubcommandAdd(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected void onEnchantCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start, WbsEnchantment enchant) {
        if (!(sender instanceof Player player)) {
            sendMessage("This command is only usable by players.", sender);
            return;
        }

        int level = 1;
        if (args.length > start) {
            String levelString = args[start];

            try {
                level = Integer.parseInt(levelString);
            } catch (NumberFormatException e) {
                sendMessage("&wInvalid level: &x" + levelString + "&w. Use an integer.", player);
                return;
            }
        }

        int maxLevel = enchant.getMaxLevel();
        if (maxLevel > 1 && maxLevel < level) {
            String unsafePermission = getPermission() + ".unsafe";
            if (!player.hasPermission(unsafePermission)) {
                sendMessage("&wYou do not have permission to apply unsafe enchantments. Permission node: &x" + unsafePermission, player);
                return;
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            sendMessage("&wHold the item to enchant!", player);
            return;
        }

        EnchantUtils.addEnchantment(enchant, item, level);
        sendMessage("Applied " + UberUtils.displayName(enchant, level) + "&r to your held item!", player);
    }

    @Override
    protected List<String> getTabCompletions(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        if (args.length == start) {
            return EnchantsSettings.getRegistered().stream()
                    .map(WbsEnchantment::getKey)
                    .map(NamespacedKey::getKey)
                    .sorted()
                    .collect(Collectors.toList());
        }

        String enchantKey = args[start - 1];

        WbsEnchantment enchant = EnchantsSettings.getRegistered().stream()
                .filter(check -> check.matches(enchantKey))
                .findFirst()
                .orElse(null);

        LinkedList<String> completions = new LinkedList<>();

        if (enchant != null) {
            switch (args.length - start) {
                case 1 -> {
                    for (int i = 0; i < enchant.getMaxLevel(); i++) {
                        completions.add(String.valueOf(i + 1));
                    }
                }
            }
        }

        return completions;
    }
}
