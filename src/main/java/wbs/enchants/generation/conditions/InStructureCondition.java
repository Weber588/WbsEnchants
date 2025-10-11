package wbs.enchants.generation.conditions;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.utils.exceptions.InvalidConfigurationException;

public class InStructureCondition extends GenerationCondition {
    public static final String KEY = "in-structure";

    @NotNull
    private final Key structureTypeKey;

    public InStructureCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        String typeString;

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            typeString = section.getString("type");
        } else {
            typeString = parentSection.getString(KEY);
        }

        if (typeString == null) {
            throw new InvalidConfigurationException("Structure type is a required field.", directory);
        }

        NamespacedKey structureKey = NamespacedKey.fromString(typeString);

        if (structureKey == null) {
            structureKey = NamespacedKey.minecraft(typeString);
        }

        structureTypeKey = structureKey;
    }

    @Override
    public boolean test(Location location) {
        return location.getChunk()
                .getStructures()
                .stream()
                .anyMatch(generated -> {
                    if (generated.getStructure().getStructureType().key().equals(structureTypeKey)) {
                        return generated.getBoundingBox().contains(location.toVector());
                    }
                    return false;
                });
    }

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        return Component.text("In structure " + structureTypeKey.asString());
    }

    @Override
    public String toString() {
        return "InStructureCondition{" +
                "type=" + structureTypeKey.asString() +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }
}
