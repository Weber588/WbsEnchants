package wbs.enchants.generation.conditions;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class HeightCondition extends RangeCondition {
    public static final String KEY = "height";

    public HeightCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);
    }

    @Override
    public boolean test(Location location) {
        return inRange(location.getBlockY());
    }
}
