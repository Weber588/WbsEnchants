package wbs.enchants.generation.conditions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;

public class EntityTypeCondition extends GenerationCondition {
    public static final String KEY = "entity-type";

    private final EntityType type;

    public EntityTypeCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        String typeString;

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            typeString = section.getString("type");
        } else {
            typeString = parentSection.getString(key);
        }

        if (typeString == null) {
            throw new InvalidConfigurationException("Specify an entity type: " +
                    WbsEnums.joiningPrettyStrings(EntityType.class), directory);
        }

        type = WbsEnums.getEnumFromString(EntityType.class, typeString);

        if (type == null) {
            throw new InvalidConfigurationException("Invalid entity type \"" + typeString + "\". Valid options: " +
                    WbsEnums.joiningPrettyStrings(EntityType.class), directory);
        }
    }

    @Override
    public boolean test(Entity entity) {
        return entity.getType() == type;
    }
}
