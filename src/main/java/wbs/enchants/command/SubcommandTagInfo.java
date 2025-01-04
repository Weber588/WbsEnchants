package wbs.enchants.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class SubcommandTagInfo extends WbsSubcommand {

    private static final Map<String, Class<? extends Keyed>> TAG_CLASSES = Map.of(
            "items", Material.class,
            "enchantment", Enchantment.class,
            "blocks", Material.class
    );

    public SubcommandTagInfo(@NotNull WbsPlugin plugin) {
        super(plugin, "taginfo");

        permission = "wbsenchants.command." + label;
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        TAG_CLASSES.forEach((key, type) ->
                builder.then(Commands.literal(key)
                        .executes(context -> this.execute(context.getSource().getSender(), key))
        ));

        builder.then(Commands.literal("custom")
                        .executes(this::executeNoArgs)
                        .then(Commands.argument("tag", ArgumentTypes.namespacedKey())
                                .executes(this::executeCustom)
                        )
        );
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        plugin.sendMessage("Usage: &h/" + context.getRootNode().getName() + " " + getLabel() + " <tag_type|\"custom\" [key]>", sender);

        return Command.SINGLE_SUCCESS;
    }

    protected int executeCustom(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        NamespacedKey namespacedKey = context.getArgument("tag", NamespacedKey.class);

        TagKey<Enchantment> tagKey = EnchantmentTagKeys.create(namespacedKey);

        io.papermc.paper.registry.tag.Tag<@NotNull Enchantment> tag = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .getTag(tagKey);

        plugin.sendMessage(tagKey.key().asString() + ":", sender);

        tag.values().forEach(entry -> plugin.sendMessage(entry.key().asString(), sender));

        return Command.SINGLE_SUCCESS;
    }

    private int execute(CommandSender sender, String key) {
        plugin.sendMessage("Key: " + key, sender);

        Class<? extends Keyed> clazz = TAG_CLASSES.get(key);
        plugin.sendMessage("Class: " + clazz.getName(), sender);

        Iterable<? extends Tag<? extends Keyed>> tags = Bukkit.getTags(key, clazz);

        List<Tag<? extends Keyed>> tagList = new LinkedList<>();
        tags.forEach(tagList::add);

        WbsMessageBuilder builder = plugin.buildMessage("Tags: &h ");
        tagList.stream().map(tag -> {
            String asString = tag.getKey().asString();
            Component component = Component.text(asString);

            String included = tag.getValues()
                    .stream()
                    .map(keyed -> keyed.getKey().toString())
                    .collect(Collectors.joining(", "));

            return component.hoverEvent(HoverEvent.showText(Component.text(included)));
        }).forEach(text -> builder.append(text).append(Component.text(", ")));
        // TODO: Fix having comma at end

        builder.send(sender);

        return Command.SINGLE_SUCCESS;
    }
}
