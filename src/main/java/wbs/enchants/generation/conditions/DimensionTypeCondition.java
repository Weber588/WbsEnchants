package wbs.enchants.generation.conditions;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchants;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class DimensionTypeCondition extends GenerationCondition {
    public static final String KEY = "dimension-type";
    private final List<World.Environment> dimensionTypes = new LinkedList<>();

    public DimensionTypeCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        List<String> typeStrings;

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            typeStrings = section.getStringList("type");
            directory = directory + "/type";
        } else {
            typeStrings = parentSection.getStringList(KEY);
        }

        if (typeStrings.isEmpty()) {
            throw new InvalidConfigurationException("Specify a dimension type: " +
                    WbsEnums.joiningPrettyStrings(World.Environment.class),
                    directory);
        }

        for (String typeString : typeStrings) {
            World.Environment dimensionType = WbsEnums.getEnumFromString(World.Environment.class, typeString);

            if (dimensionType == null) {
                WbsEnchants.getInstance().settings.logError("Invalid dimension type: \"" + typeString + "\".",
                        directory);
            } else {
                dimensionTypes.add(dimensionType);
            }
        }

        if (dimensionTypes.isEmpty()) {
            throw new InvalidConfigurationException("Specify a dimension type: " +
                    WbsEnums.joiningPrettyStrings(World.Environment.class),
                    directory);
        }
    }

    @Override
    public boolean test(Location location) {
        World.Environment dimensionType = Objects.requireNonNull(location.getWorld()).getEnvironment();
        return dimensionTypes.contains(dimensionType);
    }
}
