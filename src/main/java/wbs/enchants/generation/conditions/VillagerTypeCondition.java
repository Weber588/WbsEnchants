package wbs.enchants.generation.conditions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;

public class VillagerTypeCondition extends GenerationCondition {
    public static final String KEY = "villager-type";
    private final Villager.Type type;

    public VillagerTypeCondition(String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        String typeString;

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            typeString = section.getString("type");
        } else {
            typeString = parentSection.getString("type");
        }

        if (typeString == null) {
            throw new InvalidConfigurationException("Specify a villager type: " +
                    WbsEnums.joiningPrettyStrings(Villager.Type.class), directory);
        }

        type = WbsEnums.getEnumFromString(Villager.Type.class, typeString);

        if (type == null) {
            throw new InvalidConfigurationException("Invalid villager type \"" + typeString + "\". Valid options: " +
                    WbsEnums.joiningPrettyStrings(Villager.Type.class), directory);
        }
    }

    @Override
    public boolean test(Entity entity) {
        if (!(entity instanceof Villager villager)) {
            return false;
        }

        return villager.getVillagerType() == type;
    }
}
