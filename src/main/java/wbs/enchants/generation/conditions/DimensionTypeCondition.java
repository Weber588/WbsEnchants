package wbs.enchants.generation.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
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
            directory = directory + "/" + KEY;
        } else {
            typeStrings = parentSection.getStringList(KEY);
        }

        if (typeStrings.isEmpty()) {
            String single;
            if (section != null) {
                single = section.getString("type");
                directory = directory + "/" + KEY;
            } else {
                single = parentSection.getString(KEY);
            }

            if (single == null) {
                throw new InvalidConfigurationException("Specify a dimension type: " +
                        WbsEnums.joiningPrettyStrings(World.Environment.class),
                        directory);
            } else {
                typeStrings.add(single);
            }
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

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        List<Component> matchComponents = new LinkedList<>(
                dimensionTypes.stream().map(
                        environment -> Component.text(WbsEnums.toPrettyString(environment)
                        )).toList()
        );

        TextComponent lineStart = Component.text("\n    > ");

        return Component.text("Dimension type is in: ")
                .append(lineStart)
                .append(Component.join(JoinConfiguration.separator(lineStart), matchComponents));
    }

    @Override
    public String toString() {
        return "DimensionTypeCondition{" +
                "dimensionTypes=" + dimensionTypes +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }
}
