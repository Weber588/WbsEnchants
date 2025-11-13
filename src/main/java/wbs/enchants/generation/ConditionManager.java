package wbs.enchants.generation;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchants;
import wbs.enchants.generation.conditions.*;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.HashMap;
import java.util.Map;

public final class ConditionManager {
    private ConditionManager() {}

    private static final Map<String, RegisteredContext> registeredContexts = new HashMap<>();

    static {
        registerContext(BiomeCondition.KEY, BiomeCondition::new);
        registerContext(VillagerTypeCondition.KEY, VillagerTypeCondition::new);
        registerContext(VillagerProfessionCondition.KEY, VillagerProfessionCondition::new);
        registerContext(VillagerLevelCondition.KEY, VillagerLevelCondition::new);
        registerContext(EntityTypeCondition.KEY, EntityTypeCondition::new);
        registerContext(EntityCategoryCondition.KEY, EntityCategoryCondition::new);
        registerContext(HeightCondition.KEY, HeightCondition::new);
        registerContext(BlockTypeCondition.KEY, BlockTypeCondition::new);
        registerContext(DimensionTypeCondition.KEY, DimensionTypeCondition::new);
        registerContext(WorldCondition.KEY, WorldCondition::new);
        registerContext(InStructureCondition.KEY, InStructureCondition::new);
        registerContext(SpawnReasonCondition.KEY, SpawnReasonCondition::new);
    }

    public static void registerContext(String key, ConditionConstructor producer) {
        registeredContexts.put(key, new RegisteredContext(key, producer));
    }

    @Nullable
    public static GenerationCondition getCondition(String key, ConfigurationSection parentSection, String directory) {
        RegisteredContext registeredContext = registeredContexts.get(key);
        if (registeredContext == null) {
            WbsEnchants.getInstance().settings.logError("Condition not recognised: " + key, directory);
            return null;
        }

        try {
            return registeredContext.producer.from(key, parentSection, directory);
        } catch (InvalidConfigurationException e) {
            WbsEnchants.getInstance().settings.logError("Invalid condition: " + e.getMessage(), directory);
            return null;
        }
    }

    private record RegisteredContext(String key,
                                     ConditionConstructor producer) {
    }

    @FunctionalInterface
    public interface ConditionConstructor {
        GenerationCondition from(String key, ConfigurationSection parentSection, String directory);
    }
}
