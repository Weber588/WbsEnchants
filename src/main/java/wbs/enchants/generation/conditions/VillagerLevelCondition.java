package wbs.enchants.generation.conditions;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

public class VillagerLevelCondition extends RangeCondition {
    public static final String KEY = "villager-level";

    public VillagerLevelCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);
    }

    @Override
    public boolean test(Entity entity) {
        if (!(entity instanceof Villager villager)) {
            return false;
        }

        return inRange(villager.getVillagerLevel());
    }
}
