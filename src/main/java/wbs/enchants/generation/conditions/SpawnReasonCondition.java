package wbs.enchants.generation.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.NotNull;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;

public class SpawnReasonCondition extends GenerationCondition {
    public static final String KEY = "spawn-reason";

    private final CreatureSpawnEvent.SpawnReason reason;

    public SpawnReasonCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        String reasonString;

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            reasonString = section.getString("reason");
        } else {
            reasonString = parentSection.getString(key);
        }

        if (reasonString == null) {
            throw new InvalidConfigurationException("Specify an entity type: " +
                    WbsEnums.joiningPrettyStrings(CreatureSpawnEvent.SpawnReason.class), directory);
        }

        reason = WbsEnums.getEnumFromString(CreatureSpawnEvent.SpawnReason.class, reasonString);

        if (reason == null) {
            throw new InvalidConfigurationException("Invalid entity type \"" + reasonString + "\". Valid options: " +
                    WbsEnums.joiningPrettyStrings(CreatureSpawnEvent.SpawnReason.class), directory);
        }
    }

    @Override
    public boolean test(Entity entity) {
        return entity.getEntitySpawnReason() == reason;
    }

    @Override
    public String toString() {
        return "SpawnReasonCondition{" +
                "type=" + reason +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        return Component.text("Spawn reason " + WbsEnums.toPrettyString(reason));
    }
}
