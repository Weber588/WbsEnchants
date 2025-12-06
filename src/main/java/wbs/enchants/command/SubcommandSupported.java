package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchants;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.WbsSuggestionProvider;
import wbs.utils.util.commands.brigadier.argument.WbsStringArgumentType;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandSupported extends WbsSubcommand {
    public SubcommandSupported(@NotNull WbsPlugin plugin) {
        super(plugin, "supported");

        permission = "wbsenchants.command." + label;
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        Registry<@NotNull ItemType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM);
        builder.then(Commands.argument("item", ArgumentTypes.resourceKey(RegistryKey.ITEM))
                .suggests(new ItemTypeSuggestionProvider(((commandContext, itemType) -> !getValidEnchantments(itemType, null, registry).isEmpty())))
                .executes(commandContext -> execute(commandContext, null))
                .then(Commands.argument("namespace", WbsStringArgumentType.word())
                        .suggests(WbsSuggestionProvider.getStatic(EnchantManager.getNamespaces()))
                        .executes(this::execute)
                )
        );
    }

    protected int execute(CommandContext<CommandSourceStack> context) {
        String namespace = context.getArgument("namespace", String.class);
        return execute(context, namespace);
    }
    protected int execute(CommandContext<CommandSourceStack> context, String namespace) {
        //noinspection unchecked
        TypedKey<ItemType> itemKey = (TypedKey<ItemType>) context.getArgument("item", TypedKey.class);

        ItemType itemType = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).get(itemKey);

        if (itemType == null) {
            plugin.sendMessage("Invalid item type \"" + itemKey.asMinimalString() + "\"", context.getSource().getSender());
            return 0;
        }

        return execute(context, itemType, namespace);
    }

    protected int execute(CommandContext<CommandSourceStack> context, @NotNull ItemType itemType, @Nullable String namespace) {
        CommandSender sender = context.getSource().getSender();

        if (itemType.equals(ItemType.BOOK) || itemType.equals(ItemType.ENCHANTED_BOOK)) {
            plugin.sendMessage("Books support all enchantments.", sender);
            return Command.SINGLE_SUCCESS;
        }

        Registry<@NotNull ItemType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM);
        List<EnchantmentDefinition> supported = getValidEnchantments(itemType, namespace, registry);

        Collection<Enchantment> inEnchantingTable = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getTagValues(EnchantmentTagKeys.IN_ENCHANTING_TABLE);

        boolean isEnchantable = isEnchantable(itemType);

        List<EnchantmentDefinition> tableEnchants = new LinkedList<>();
        if (isEnchantable) {
            tableEnchants = new LinkedList<>(EnchantManager.getAllKnownDefinitions()
                    .stream()
                    .filter(enchantment -> {
                        Enchantment serverEnchantment = enchantment.getEnchantment();
                        if (!inEnchantingTable.contains(serverEnchantment)) {
                            return false;
                        }

                        return EnchantUtils.isPrimaryItem(itemType, serverEnchantment);
                    })
                    .filter(enchantment -> namespace == null || enchantment.key().namespace().equals(namespace))
                    .toList());
        }

        String namespaceMessage = namespace == null ? "" : " from " + namespace;

        if (supported.isEmpty()) {
            plugin.buildMessage("")
                    .append(Component.translatable(itemType.translationKey(), itemType.key().asMinimalString()))
                    .append(" does not support any enchantments" + namespaceMessage + ".")
                    .send(sender);
        } else {
            supported.sort(EnchantmentDefinition::compareTo);

            WbsMessageBuilder builder = plugin.buildMessage("")
                    .append(Component.translatable(itemType.translationKey(), itemType.key().asMinimalString()))
                    .append(" supports the following enchantment(s)" + namespaceMessage + ":\n"
            );

            appendEnchants(supported, tableEnchants, builder);

            builder.build().send(sender);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static @NotNull List<EnchantmentDefinition> getValidEnchantments(@NotNull ItemType itemType, @Nullable String namespace, Registry<@NotNull ItemType> registry) {
        return new LinkedList<>(EnchantManager.getAllKnownDefinitions()
                .stream()
                .filter(enchantment -> enchantment.getEnchantment()
                        .getSupportedItems()
                        .contains(RegistryKey.ITEM.typedKey(itemType.key()))
                )
                .filter(enchantment -> namespace == null || enchantment.key().namespace().equals(namespace))
                .toList());
    }

    private static boolean isEnchantable(@NotNull ItemType itemType) {
        return itemType.hasDefaultData(DataComponentTypes.ENCHANTABLE) || (WbsEnchants.getInstance().getSettings().addEnchantability() && EnchantsSettings.isPrimaryItem(itemType));
    }

    private static void appendEnchants(List<EnchantmentDefinition> supported, List<EnchantmentDefinition> onTable, WbsMessageBuilder builder) {
        EnchantmentDefinition first = supported.getFirst();
        supported.removeFirst();
        TextDecoration primaryDeco = TextDecoration.UNDERLINED;
        builder.append(first.interactiveDisplay().decorationIfAbsent(primaryDeco, TextDecoration.State.byBoolean(onTable.contains(first))));

        supported.forEach(enchant -> {
            builder.append("&r, ");
            builder.append(enchant.interactiveDisplay().decorationIfAbsent(primaryDeco, TextDecoration.State.byBoolean(onTable.contains(enchant))));
        });
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("&cSpecify an item type.", sender);
            return 0;
        }

        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.getType() == Material.AIR) {
            plugin.sendMessage("Hold an item to view enchantments applicable to it!", sender);
            return 0;
        }

        return execute(
                context,
                Objects.requireNonNull(held.getType().asItemType(), "Held item did not have an item type?"),
                null
        );
    }
}
