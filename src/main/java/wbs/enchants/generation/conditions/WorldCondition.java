package wbs.enchants.generation.conditions;

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
        if (name != null && location.getWorld().getName().equalsIgnoreCase(name)) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "WorldCondition{" +
                "name='" + name + '\'' +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }
}
