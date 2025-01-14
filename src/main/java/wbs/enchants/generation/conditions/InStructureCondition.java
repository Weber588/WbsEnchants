package wbs.enchants.generation.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.structure.StructureType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.stream.Collectors;

public class InStructureCondition extends GenerationCondition {
    public static final String KEY = "in-structure";

    @NotNull
    private final StructureType type;

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
            throw new InvalidConfigurationException("Specify a structure type: " +
                    Registry.STRUCTURE_TYPE.stream()
                            .map(structure -> structure.getKey().toString())
                            .collect(Collectors.joining(", ")),
                    directory);
        }

        NamespacedKey structureKey = NamespacedKey.fromString(typeString);

        if (structureKey == null) {
            structureKey = NamespacedKey.minecraft(typeString);
        }

        StructureType check = Registry.STRUCTURE_TYPE.get(structureKey);
        if (check != null) {
            type = check;
        } else {
            throw new InvalidConfigurationException("Specify a structure type: " +
                    Registry.STRUCTURE_TYPE.stream()
                            .map(structure -> structure.getKey().toString())
                            .collect(Collectors.joining(", ")),
                    directory);
        }
    }

    @Override
    public boolean test(Location location) {
        return location.getChunk()
                .getStructures()
                .stream()
                .anyMatch(generated -> {
                    if (generated.getStructure().getStructureType() == type) {
                        return generated.getBoundingBox().contains(location.toVector());
                    }
                    return false;
                });
    }

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        return Component.text("In structure " + type.key());
    }

    @Override
    public String toString() {
        return "InStructureCondition{" +
                "type=" + type +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }
}
