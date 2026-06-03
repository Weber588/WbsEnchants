package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SubcommandList extends WbsSubcommand {
    private static final WbsSimpleArgument<String> NAMESPACE_FILTER = new WbsSimpleArgument<>("namespace",
            StringArgumentType.word(),
            "",
            String.class
    ).setSuggestions(EnchantManager.getNamespaces());
    private static final WbsSimpleArgument.KeyedSimpleArgument ENCHANTMENT_TYPE = (WbsSimpleArgument.KeyedSimpleArgument)
            new WbsSimpleArgument.KeyedSimpleArgument("attribute_key",
                    ArgumentTypes.namespacedKey(),
                    null
            ).setSuggestions(EnchantmentTypeManager.getRegistered().stream().map(Keyed::getKey).toList());

    public SubcommandList(@NotNull WbsPlugin plugin) {
        super(plugin, "list");

        permission = "wbsenchants.command." + label;

        addSimpleArgument(NAMESPACE_FILTER);
        addSimpleArgument(ENCHANTMENT_TYPE);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        CommandSender sender = context.getSource().getSender();

        String filter = configuredArgumentMap.get(NAMESPACE_FILTER);
        NamespacedKey enchantmentTypeKey = configuredArgumentMap.get(ENCHANTMENT_TYPE);

        boolean enchantsExistInNamespace = EnchantManager.getDefinitionsByNamespace().containsKey(filter);

        EnchantmentType type = null;
        if (enchantmentTypeKey != null) {
            type = EnchantmentTypeManager.getType(enchantmentTypeKey, null);
            if (type == null) {
                plugin.sendMessage("Invalid enchant type \"%s\"".formatted(enchantmentTypeKey.asMinimalString()), sender);
                return Command.SINGLE_SUCCESS;
            }
        }
        if (type == null && !enchantsExistInNamespace) {
            // No type given, and no enchants in given namespace -- try parsing filter as a key & using instead.
            enchantmentTypeKey = NamespacedKey.fromString(filter, plugin);

            if (enchantmentTypeKey != null) {
                type = EnchantmentTypeManager.getType(enchantmentTypeKey, null);
                if (type == null) {
                    plugin.sendMessage(enchantmentTypeKey + " did not match any enchantment namespaces or enchantment types.", sender);
                    return Command.SINGLE_SUCCESS;
                }
            } // If no type, just continue and show all

            filter = null;
        }

        if (filter != null) {
            sendEnchantmentList(sender, filter, type);
        } else {
            for (String namespace : EnchantManager.getNamespaces()) {
                sendEnchantmentList(sender, namespace, type);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private void sendEnchantmentList(CommandSender sender, @NotNull String namespace, @Nullable EnchantmentType type) {
        List<EnchantmentDefinition> enchants = EnchantManager.getAllKnownDefinitions()
                .stream()
                // TODO: Add a "show-in-commands" field on enchantment def, so admins can manually hide util/backend enchants like "Illegal enchantment" or "I AM ERROR"
                .filter(def -> !def.key().value().contains("_technical"))
                .sorted()
                .collect(Collectors.toCollection(LinkedList::new));

        enchants.removeIf(def -> !def.key().namespace().equalsIgnoreCase(namespace));

        if (type != null) {
            enchants.removeIf(def -> !def.getType().equals(type));
        }

        if (enchants.isEmpty()) {
            TextComponent error;
            if (type != null) {
                error = Component.text(namespace + " does not have any enchantments of type ").append(type.getNameComponent());
            } else {
                error = Component.text("No enchantments found!");
            }
            plugin.buildMessage(error).send(sender);
            return;
        }

        enchants.sort(EnchantmentDefinition::compareTo);

        Component opening = Component.text(namespace).append(Component.text(" enchantments (" + enchants.size() + "):\n")).color(plugin.getTextColour());

        WbsMessageBuilder builder = plugin.buildMessage("").append(opening);

        EnchantmentDefinition first = enchants.getFirst();
        enchants.removeFirst();
        builder.append(first.interactiveDisplay());

        enchants.forEach(enchant -> {
            builder.append("&r, ");
            builder.append(enchant.interactiveDisplay());
        });

        builder.build().send(sender);
    }
}
