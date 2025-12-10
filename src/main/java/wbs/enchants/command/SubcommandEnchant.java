package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantManager;
import wbs.enchants.events.enchanting.EnchantingContext;
import wbs.enchants.util.EnchantingEventUtils;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandEnchant extends WbsSubcommand {
    public SubcommandEnchant(@NotNull WbsPlugin plugin) {
        super(plugin, "enchant");

        permission = "wbsenchants.command." + label;
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {

        builder.then(Commands.argument("level", IntegerArgumentType.integer(0))
                .suggests((context, suggestions) -> {
                    suggestions.suggest(1)
                            .suggest(10)
                            .suggest(20)
                            .suggest(30);

                    if (EnchantManager.AMBITIOUSNESS.isEnabled()) {
                        suggestions.suggest(60);
                    }

                    return suggestions.buildFuture();
                })
                .executes(commandContext -> execute(commandContext, false))
                .then(Commands.argument("enchantAsLoot", BoolArgumentType.bool())
                        .suggests((context, suggestions) -> {
                            suggestions.suggest("true", () -> "enchantAsLoot");
                            suggestions.suggest("false", () -> "enchantAsLoot");
                            return suggestions.buildFuture();
                        })
                        .executes(this::execute)
                )
        );
    }

    protected int execute(CommandContext<CommandSourceStack> context) {
        boolean enchantAsLoot = context.getArgument("enchantAsLoot", Boolean.class);
        return execute(context, enchantAsLoot);
    }
    protected int execute(CommandContext<CommandSourceStack> context, boolean enchantAsLoot) {
        int level = context.getArgument("level", Integer.class);

        return execute(context, level, enchantAsLoot);
    }

    protected int execute(CommandContext<CommandSourceStack> context, int level, boolean enchantAsLoot) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return Command.SINGLE_SUCCESS;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.isEmpty()) {
            plugin.sendMessage("Hold an item to enchant!", sender);
            return Command.SINGLE_SUCCESS;
        }

        if (!heldItem.getEnchantments().isEmpty()) {
            plugin.sendMessage("The held item is already enchanted. You can use &h/cench clear&r to clear all enchantments if needed, or hold a different item.", sender);
            return Command.SINGLE_SUCCESS;
        }

        TagKey<Enchantment> tag = EnchantmentTagKeys.IN_ENCHANTING_TABLE;
        if (enchantAsLoot) {
            tag = EnchantmentTagKeys.ON_RANDOM_LOOT;
        }

        Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        Collection<@NotNull Enchantment> availableEnchants = new LinkedList<>(registry.getTag(tag).resolve(registry));

        if (availableEnchants.isEmpty()) {
            plugin.sendMessage("No enchantments were present in the " + tag.key().asMinimalString() + " tag!", sender);
            return Command.SINGLE_SUCCESS;
        }

        int seed = new Random().nextInt();
        EnchantingContext enchantingContext = new EnchantingContext(player.getLocation().getBlock(), player, heldItem, seed, List.of());
        Map<Enchantment, Integer> enchantments = EnchantingEventUtils.finalizeSelection(enchantingContext, heldItem, 3, level, seed, availableEnchants);

        if (enchantments.isEmpty()) {
            Set<Enchantment> filteredAvailable = EnchantingEventUtils.getAvailableEnchants(enchantingContext, heldItem, availableEnchants);

            if (filteredAvailable.isEmpty()) {
                plugin.sendMessage("No enchantments were available for the held item. ", sender);
                return Command.SINGLE_SUCCESS;
            }

            int modifiedLevel = EnchantingEventUtils.getModifiedLevel(seed, 3, level, heldItem);
            if (modifiedLevel < 0) {
                plugin.sendMessage("Item is not enchantable.", sender);
                return Command.SINGLE_SUCCESS;
            }

            Map<Enchantment, Integer> availableEnchantmentAtLevel = EnchantingEventUtils.getAvailableEnchantmentAtLevel(enchantingContext, modifiedLevel, heldItem, availableEnchants);
            if (availableEnchantmentAtLevel.isEmpty()) {
                plugin.sendMessage("No enchantments were available at the level you specified.", sender);
                return Command.SINGLE_SUCCESS;
            }

            plugin.sendMessage("No enchantments were able to be added for an unknown reason. Available at level: " +
                    availableEnchantmentAtLevel.keySet().stream().map(ench -> ench.key().asMinimalString())
                            .collect(Collectors.joining(", ")), sender);
            return Command.SINGLE_SUCCESS;
        }

        heldItem.addEnchantments(enchantments);
        player.getWorld().playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.UI, 1, 1);

        plugin.sendMessage("Simulated enchanting item at level " + level + " (" + (enchantAsLoot ? "loot" : "enchanting table") + ")", sender);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return 0;
        }

        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.isEmpty()) {
            plugin.sendMessage("Hold an item to enchant.", sender);
            return 0;
        }

        return execute(context, 30, false);
    }
}
