package wbs.enchants.generation.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class WorldCondition extends GenerationCondition {
    public static final String KEY = "world";

    private final String name;

    public WorldCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        ConfigurationSection section = parentSection.getConfigurationSection(key);

        if (section != null) {
            name = section.getString("name");
        } else {
            name = parentSection.getString(key);
        }
    }

    @Override
    public boolean test(Location location) {
        return name != null && location.getWorld().getName().equalsIgnoreCase(name);
    }

    @Override
    public String toString() {
        return "WorldCondition{" +
                "name='" + name + '\'' +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        return Component.text("World name = " + name);
    }
}
