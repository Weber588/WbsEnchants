package wbs.enchants.generation.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class BiomeCondition extends GenerationCondition {
    public static final String KEY = "biome";
    
    private List<String> matches;
    private double minTemp = Double.MIN_VALUE;
    private double maxTemp = Double.MAX_VALUE;
    private double minHumidity = Double.MIN_VALUE;
    private double maxHumidity = Double.MAX_VALUE;

    public BiomeCondition(String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            // min-temp, max-temp, min-humidity, max-humidity, matches
            minTemp = section.getDouble("min-temp", minTemp);
            maxTemp = section.getDouble("max-temp", maxTemp);
            minHumidity = section.getDouble("min-humidity", minHumidity);
            maxHumidity = section.getDouble("max-humidity", maxHumidity);

            matches = section.getStringList("matches");
        } else {
            if (parentSection.isList(KEY)) {
                matches = parentSection.getStringList(KEY);
            } else {
                matches = List.of(Objects.requireNonNull(parentSection.getString(KEY)));
            }
        }
    }

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        Component matchesComponent = null;
        Component matchesListComponent = null;
        if (!matches.isEmpty()) {
            matchesComponent = Component.text("Biome matches any of the below ")
                    .append(Component.text("RegEx")
                            .clickEvent(ClickEvent.openUrl("https://regexone.com"))
                            .hoverEvent(HoverEvent.showText(Component.text("RegEx is a method for identifying words or phrases with a simple string. Click for more information.")))
                    );

            List<TextComponent> matchesComponents = matches.stream()
                    .map(Component::text)
                    .toList();

            matchesListComponent = listBreak.append(Component.join(JoinConfiguration.separator(listBreak), matchesComponents));
        }

        Component humidityComponent = null;
        if (minHumidity != Double.MIN_VALUE) {
            if (maxHumidity != Double.MAX_VALUE) {
                humidityComponent = Component.text("Humidity: " + minHumidity + "-" + maxHumidity);
            } else {
                humidityComponent = Component.text("Humidity: >" + minHumidity);
            }
        } else if (maxHumidity != Double.MAX_VALUE) {
            humidityComponent = Component.text("Humidity: <" + maxHumidity);
        }

        Component tempComponent = null;
        if (minTemp != Double.MIN_VALUE) {
            if (maxTemp != Double.MAX_VALUE) {
                tempComponent = Component.text("Temperature: " + minTemp + "-" + maxTemp);
            } else {
                tempComponent = Component.text("Temperature: >" + minTemp);
            }
        } else if (maxTemp != Double.MAX_VALUE) {
            tempComponent = Component.text("Temperature: <" + maxTemp);
        }
        Component humidityTempComponent = null;
        if (humidityComponent != null) {
            humidityTempComponent = humidityComponent;
            if (tempComponent != null) {
                humidityTempComponent = humidityTempComponent.append(Component.text(" & ")).append(tempComponent);
            }
        } else if (tempComponent != null) {
            humidityTempComponent = tempComponent;
        }

        if (matchesComponent != null) {
            if (humidityTempComponent != null) {
                return matchesComponent.append(Component.text(", and "))
                        .append(humidityTempComponent)
                        .append(matchesListComponent);
            } else {
                return matchesComponent
                        .append(matchesListComponent);
            }
        } else if (humidityTempComponent != null) {
            return humidityTempComponent;
        }

        throw new IllegalStateException("Biome Condition lacked required information (temp/humidity/biome match info)");
    }

    @Override
    public boolean test(Location location) {
        double temp = location.getBlock().getTemperature();
        double humidity = location.getBlock().getHumidity();

        World world = location.getWorld();

        if (world == null) {
            return true;
        }

        boolean inValidBiome = true;
        if (!matches.isEmpty()) {
            String biomeKey = world.getBiome(location).getKey().asString();

            boolean matchFound = matches.stream()
                    .anyMatch(regex -> biomeKey.matches(regex) || biomeKey.contains(regex));

            if (!matchFound) {
                inValidBiome = false;
            }
        }

        return inValidBiome && temp <= maxTemp && temp >= minTemp &&
                humidity <= maxHumidity && humidity >= minHumidity;
    }

    @Override
    public String toString() {
        return "BiomeCondition{" +
                "maxHumidity=" + maxHumidity +
                ", key=" + key +
                ", matches=" + matches +
                ", minTemp=" + minTemp +
                ", maxTemp=" + maxTemp +
                ", minHumidity=" + minHumidity +
                ", negated=" + negated +
                '}';
    }
}
