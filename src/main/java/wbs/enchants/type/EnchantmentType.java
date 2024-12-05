package wbs.enchants.type;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantsBootstrap;

public abstract class EnchantmentType implements Keyed, Comparable<EnchantmentType> {
    protected final String name;
    protected final NamespacedKey key;

    public EnchantmentType(String name, NamespacedKey key) {
        this.name = name;
        this.key = key;
    }

    EnchantmentType(String name, String key) {
        this(name, new NamespacedKey(WbsEnchantsBootstrap.NAMESPACE, key));
    }

    EnchantmentType(String name) {
        this(name, name.toLowerCase().replaceAll("\\s", "_"));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    public abstract @NotNull Component getDescription();
    public @Nullable TagKey<Enchantment> getTagKey() {
        return TagKey.create(RegistryKey.ENCHANTMENT, getKey());
    }

    public void registerListeners() {

    }

    public abstract TextColor getColour();

    public boolean matches(String search) {
        if (search == null) {
            return false;
        }
        if (search.equalsIgnoreCase(key.asString())) {
            return true;
        }
        if (search.equalsIgnoreCase(key.value())) {
            return true;
        }

        return false;
    }

    public String getName() {
        return name;
    }

    public Component getNameComponent() {
        return Component.text(getName())
                .color(getColour())
                .hoverEvent(
                        HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, getDescription())
                );
    }

    public int compareTo(EnchantmentType type) {
        return getKey().compareTo(type.getKey());
    }
}
