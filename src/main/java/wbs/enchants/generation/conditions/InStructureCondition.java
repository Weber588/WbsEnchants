package wbs.enchants.generation.conditions;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.generator.structure.Structure;
import org.bukkit.util.StructureSearchResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.stream.Collectors;

public class InStructureCondition extends GenerationCondition {
    public static final String KEY = "in-structure";

    @Nullable
    private final Structure structure;
    @NotNull
    private final StructureType type;

    protected InStructureCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        String typeString;

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            typeString = section.getString("type");
        } else {
            typeString = parentSection.getString("type");
        }

        if (typeString == null) {
            throw new InvalidConfigurationException("Specify a structure type: " +
                    Registry.STRUCTURE.stream()
                            .map(structure -> structure.getKey().toString())
                            .collect(Collectors.joining(", ")),
                    directory);
        }

        NamespacedKey structureKey = NamespacedKey.fromString(typeString);

        if (structureKey == null) {
            structureKey = NamespacedKey.minecraft(typeString);
        }

        structure = Registry.STRUCTURE.get(structureKey);

        if (structure != null) {
            type = structure.getStructureType();
        } else {
            StructureType type = Registry.STRUCTURE_TYPE.get(structureKey);
            if (type != null) {
                this.type = type;
            } else {
                throw new InvalidConfigurationException("Invalid villager type \"" + typeString + "\". Valid options: " +
                        Registry.STRUCTURE.stream()
                                .map(structure -> structure.getKey().toString())
                                .collect(Collectors.joining(", ")),
                        directory);
            }
        }
    }

    @Override
    public boolean test(Location location) {

        // why no work </3
        // Chunk chunk = location.getChunk();
        // GeneratedStructure structure = chunk.getStructures();

        // Hacky way for now
        World world = location.getWorld();
        if (world == null) {
            return false;
        }

        StructureSearchResult structureSearchResult;
        if (structure != null) {
            structureSearchResult = world.locateNearestStructure(location, structure, 16, false);
        } else {
            structureSearchResult = world.locateNearestStructure(location, type, 16, false);
        }

        return structureSearchResult != null;
    }
}
