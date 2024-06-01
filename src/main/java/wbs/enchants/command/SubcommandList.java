package wbs.enchants.command;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.WbsSubcommand;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.RomanNumerals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SubcommandList extends WbsSubcommand {
    public SubcommandList(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected final boolean onCommand(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, int start) {
        SortBy sortBy = SortBy.NONE;
        if (args.length > start) {
            String sortByString = args[start];

            sortBy = WbsEnums.getEnumFromString(SortBy.class, sortByString);

            if (sortBy == null) {
                sendMessage("Invalid sort mode \"&h" + sortByString + "&r\". Please choose from the following: " +
                        WbsEnums.joiningPrettyStrings(SortBy.class), sender);
                return true;
            }
        }

        List<WbsEnchantment> enchants = EnchantsSettings.getRegistered()
                .stream()
                .sorted()
                .collect(Collectors.toList());

        if (enchants.isEmpty()) {
            sendMessage("No enchantments enabled!", sender);
            return true;
        }

        WbsMessageBuilder builder = new WbsMessageBuilder(plugin, "Enchantments: ");

        switch (sortBy) {
            case NONE -> {
                WbsEnchantment first = enchants.get(0);
                enchants.remove(0);
                appendEnchant(builder, first);

                enchants.forEach(enchant -> {
                    builder.append("&r, ");
                    appendEnchant(builder, enchant);
                });
            }
            case RARITY -> {
                for (Rarity rarity : Rarity.values()) {
                    List<WbsEnchantment> inRarity = enchants.stream()
                            .filter(ench -> ench.getRarity() == rarity)
                            .collect(Collectors.toList());

                    if (inRarity.isEmpty()) {
                        continue;
                    }

                    builder.append("\n");

                    WbsEnchantment first = inRarity.get(0);
                    inRarity.remove(0);
                    appendEnchant(builder, first);

                    for (WbsEnchantment enchant : inRarity) {
                        if (enchant.getRarity() == rarity) {
                            builder.append("&r, ");
                            appendEnchant(builder, enchant);
                        }
                    }
                }
            }
            case ITEM_TARGET -> {
                Set<String> uniqueValues = new HashSet<>();
                for (WbsEnchantment enchantment : enchants) {
                    String target = enchantment.getTargetDescription();
                    if (uniqueValues.add(target)) {
                        for (WbsEnchantment enchant : enchants) {
                            if (enchant.getTargetDescription().equals(target)) {
                                builder.append("&r, ");
                                appendEnchant(builder, enchant);
                            }
                        }
                    }
                }
            }
        }

        builder.build().send(sender);

        return true;
    }

    private static String getKeyDisplay(WbsEnchantment enchantment) {
        String keyString = enchantment.getKey().getKey();

        if (enchantment.isCursed()) {
            keyString = "&c" + keyString;
        } else {
            keyString = "&7" + keyString;
        }

        return keyString + "&r";
    }

    private static String getHoverText(WbsEnchantment enchant) {
        String text = "&h&m        &h " + enchant.getDisplayName() + "&h &m        ";

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

    private enum SortBy {
        NONE,
        RARITY,
        ITEM_TARGET,
    }
}
