package wbs.enchants.generation.conditions;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchants;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class BlockTypeCondition extends GenerationCondition {
    public static final String KEY = "block-type";

    private final List<Material> exactMatches = new LinkedList<>();
    private final List<Key> tagMatches = new LinkedList<>();
    private final List<String> tagStrings = new LinkedList<>();

    public BlockTypeCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        List<String> matches;
        if (parentSection.isConfigurationSection(key)) {
            ConfigurationSection section = parentSection.getConfigurationSection(key);

            matches = Objects.requireNonNull(section).getStringList("matches");
            directory += "/matches";
        } else {
            if (parentSection.isList(KEY)) {
                matches = parentSection.getStringList(KEY);
            } else {
                matches = List.of(Objects.requireNonNull(parentSection.getString(KEY)));
            }
        }

        for (String check : matches) {
            Material found = WbsEnums.getEnumFromString(Material.class, check);
            if (found != null) {
                exactMatches.add(found);
            }

            NamespacedKey tagKey = NamespacedKey.fromString(check);
            if (tagKey == null) {
                tagKey = NamespacedKey.minecraft(check);
                tagMatches.add(tagKey);
                tagStrings.add(check);
            }
        }
        if (exactMatches.isEmpty() && tagMatches.isEmpty()) {
            throw new InvalidConfigurationException("No valid materials or tags provided! Disabling condition.", directory);
        }
    }

    @Override
    public boolean test(Location location) {
        Material type = location.getBlock().getType();

        if (exactMatches.contains(type)) {
            return true;
        }

        RegistryKey<BlockType> registryKey = RegistryKey.BLOCK;
        Registry<@NotNull BlockType> registry = RegistryAccess.registryAccess().getRegistry(registryKey);
        for (Key key : tagMatches) {
            TagKey<BlockType> tagKey = TagKey.create(registryKey, key);

            if (!registry.hasTag(tagKey)) {
                WbsEnchants.getInstance().getLogger().severe("An invalid tag was present at runtime! Please report this -- " +
                        key.asString());
                continue;
            }

            Tag<@NotNull BlockType> tag = registry.getTag(tagKey);
            if (tag.contains(TypedKey.create(registryKey, type.asBlockType().key()))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        List<Component> matchComponents = new LinkedList<>(
                exactMatches.stream().map(material -> {
                    String translationKey = material.getBlockTranslationKey();
                    if (translationKey == null) {
                        return Component.text(material.name());
                    }
                    return Component.translatable(translationKey);
                }).toList()
        );

        matchComponents.addAll(
                tagStrings.stream().map(Component::text).toList()
        );

        return Component.text("Block type is in: ")
                .append(listBreak)
                .append(Component.join(JoinConfiguration.separator(listBreak), matchComponents));
    }

    @Override
    public String toString() {
        return "BlockTypeCondition{" +
                "exactMatches=" + exactMatches +
                ", tagMatches=" + tagMatches +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }
}
