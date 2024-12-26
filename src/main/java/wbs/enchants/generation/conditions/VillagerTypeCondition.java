package wbs.enchants.generation.conditions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsKeyed;

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
            typeString = parentSection.getString(key);
        }

        if (typeString == null) {
            throw new InvalidConfigurationException("Specify a villager type: " +
                    WbsKeyed.joiningPrettyStrings(Villager.Type.class), directory);
        }

        type = WbsKeyed.getKeyedFromString(Villager.Type.class, typeString);

        if (type == null) {
            throw new InvalidConfigurationException("Invalid villager type \"" + typeString + "\". Valid options: " +
                    WbsKeyed.joiningPrettyStrings(Villager.Type.class), directory);
        }
    }

    @Override
    public boolean test(Entity entity) {
        if (!(entity instanceof Villager villager)) {
            return false;
        }

        return villager.getVillagerType() == type;
    }

    @Override
    public String toString() {
        return "VillagerTypeCondition{" +
                "type=" + type +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }
}
