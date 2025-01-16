package wbs.enchants.definition;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class TaggableRegistryKeySet<T extends Keyed> {
    @NotNull
    private final RegistryKey<T> registryKey;

    private RegistryKeySet<@NotNull T> keySet;
    @Nullable
    private TagKey<T> tagKey;


    public TaggableRegistryKeySet(@NotNull RegistryKeySet<@NotNull T> keySet) {
        this.registryKey = keySet.registryKey();
        this.keySet = keySet;
    }
    public TaggableRegistryKeySet(@NotNull TagKey<T> tagKey) {
        this.registryKey = tagKey.registryKey();
        this.tagKey = tagKey;
    }

    @Nullable
    public TagKey<T> getTagKey() {
        return tagKey;
    }

    @Nullable
    @Contract("!null -> !null")
    public RegistryKeySet<@NotNull T> getKeySet(@Nullable EnchantmentDefinition.TagProducer tagProducer) {
        if (keySet == null && tagKey == null) {
            throw new IllegalStateException("tagKey or keySet must be populated.");
        }

        if (keySet != null) {
            return keySet;
        }

        if (tagProducer != null) {
            keySet = tagProducer.getOrCreateTag(tagKey);
            return keySet;
        }

        return null;
    }

    @Nullable
    @Contract("!null -> !null")
    public Collection<@NotNull TypedKey<T>> getTypedKeys(@Nullable EnchantmentDefinition.TagProducer tagProducer) {
        RegistryKeySet<@NotNull T> found = getKeySet(tagProducer);

        if (found == null) {
            return null;
        }

        return found.values();
    }

    @Nullable
    @Contract("!null -> !null")
    public Collection<@NotNull T> getValues(@Nullable EnchantmentDefinition.TagProducer tagProducer) {
        Collection<@NotNull TypedKey<T>> typedKeys = getTypedKeys(tagProducer);

        if (typedKeys == null) {
            return null;
        }

        Registry<T> registry = RegistryAccess.registryAccess().getRegistry(registryKey);

        return typedKeys.stream().map(registry::get).filter(Objects::nonNull).toList();
    }

    public @NotNull Component getDisplay(Component lineStart, Function<@NotNull T, @NotNull Component> tToComponent) {
        // For some reason functional interfaces don't allow generics :( Using anon class instead.
        EnchantmentDefinition.TagProducer producer = new EnchantmentDefinition.TagProducer() {
            @Override
            public <V extends Keyed> Tag<@NotNull V> getOrCreateTag(TagKey<V> tagKey) {
                return RegistryAccess.registryAccess().getRegistry(tagKey.registryKey()).getTag(tagKey);
            }
        };

        Collection<@NotNull T> values = getValues(producer);

        List<Component> componentList = values.stream().map(tToComponent).toList();
        if (tagKey != null) {
            TextComponent display = Component.text("#" + tagKey.key().asString());

            Component listComponent = Component.join(JoinConfiguration.separator(Component.text(", ")), componentList);
            display = display.hoverEvent(HoverEvent.showText(listComponent));

            return display;
        } else {
            return Component.join(JoinConfiguration.separator(Component.newline().append(lineStart)), componentList);
        }
    }

    public void writeToConfig(ConfigurationSection section, String key) {
        if (tagKey != null) {
            section.set(key, tagKey.key().toString());
        } else if (keySet != null && !keySet.isEmpty()) {
            List<String> keyStrings = keySet.values().stream()
                    .map(TypedKey::key)
                    .map(Key::asString)
                    .toList();

            section.set(key, keyStrings);
        }
    }

    public boolean isEmpty() {
        if (tagKey != null) {
            // Mainly for WbsEnchantsBootstrap#ITEM_EMPTY (wbsenchants:empty) but this should support other datapack ones too.
            return tagKey.key().value().equalsIgnoreCase("empty");
        }

        return keySet.isEmpty();
    }

    public static class TaggableItemKeySet extends TaggableRegistryKeySet<ItemType> {
        public TaggableItemKeySet(@NotNull RegistryKeySet<@NotNull ItemType> keySet) {
            super(keySet);
        }

        public TaggableItemKeySet(@NotNull TagKey<ItemType> tagKey) {
            super(tagKey);
        }

        public @NotNull Component getDisplay(Component lineStart) {
            return getDisplay(lineStart, itemType -> Component.translatable(itemType.translationKey()));
        }
    }
}
