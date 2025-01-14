package wbs.enchants.generation;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.generation.contexts.*;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.HashMap;
import java.util.Map;

public final class ContextManager {
    private ContextManager() {}

    private static final Map<String, ContextConstructor> registeredContexts = new HashMap<>();

    static {
        registerContext("loot-table", LootTableContext::new);
        registerContext("trading", VillagerTradeContext::new);
        registerContext("death", MobDeathContext::new);
        registerContext("spawn", MobSpawnContext::new);
        registerContext("bartering", BarterContext::new);
        registerContext("fishing", FishingContext::new);
        registerContext("loot-replace", LootTableReplacementContext::new);
    }
    
    public static void registerContext(String key, ContextConstructor producer) {
        registeredContexts.put(key, producer);
    }

    @NotNull
    public static GenerationContext getContext(String key,
                                               EnchantmentDefinition definition,
                                               ConfigurationSection section,
                                               String directory) {
        ContextConstructor registeredContext = registeredContexts.get(key);
        if (registeredContext == null) {
            throw new InvalidConfigurationException("Invalid context key: " + key, directory);
        }

        return registeredContext.from(key, definition, section, directory);
    }

    @FunctionalInterface
    public interface ContextConstructor {
        GenerationContext from(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory);
    }
}
