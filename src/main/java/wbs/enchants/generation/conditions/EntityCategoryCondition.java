package wbs.enchants.generation.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;

// TODO: Convert this class to be "EntityTagCondition" to check if an entity type is in a provided tag
public class EntityCategoryCondition extends GenerationCondition {
    public static final String KEY = "entity-category";

    private final EntityCategory type;

    public EntityCategoryCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        String typeString;

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            typeString = section.getString("category");
        } else {
            typeString = parentSection.getString(key);
        }

        if (typeString == null) {
            throw new InvalidConfigurationException("Specify an entity category: " +
                    WbsEnums.joiningPrettyStrings(EntityCategory.class), directory);
        }

        type = WbsEnums.getEnumFromString(EntityCategory.class, typeString);

        if (type == null) {
            throw new InvalidConfigurationException("Invalid entity type \"" + typeString + "\". Valid options: " +
                    WbsEnums.joiningPrettyStrings(EntityCategory.class), directory);
        }
    }

    @Override
    public boolean test(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }

        return living.getCategory() == type;
    }

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        return Component.text("Entity category is " + WbsEnums.toPrettyString(type));
    }

    @Override
    public String toString() {
        return "EntityCategoryCondition{" +
                "type=" + type +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }
}
