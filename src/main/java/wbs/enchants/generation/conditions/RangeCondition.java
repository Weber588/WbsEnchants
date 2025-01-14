package wbs.enchants.generation.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.string.WbsStrings;

public abstract class RangeCondition extends GenerationCondition {
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;

    public RangeCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            min = section.getInt("min", min);
            max = section.getInt("max", max);
        } else {
            int exact = parentSection.getInt(key, 0);

            min = exact;
            max = exact;
        }
    }

    protected boolean inRange(int value) {
        return value <= max && value >= min;
    }

    @Override
    public String toString() {
        return "RangeCondition{" +
                "min=" + min +
                ", max=" + max +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        Component rangeComponent = Component.empty();
        if (min != Integer.MIN_VALUE) {
            if (max != Integer.MAX_VALUE) {
                rangeComponent = Component.text( min + "-" + max);
            } else {
                rangeComponent = Component.text(">" + min);
            }
        } else if (max != Integer.MAX_VALUE) {
            rangeComponent = Component.text(" <" + max);
        }

        return Component.text(WbsStrings.capitalizeAll(key.value().replaceAll("[-_]", " ")))
                .append(Component.text(" "))
                .append(rangeComponent);
    }
}
