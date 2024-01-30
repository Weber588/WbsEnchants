package wbs.enchants.generation;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.generation.contexts.*;

import java.util.HashMap;
import java.util.Map;

public final class ContextManager {
    private ContextManager() {}

    private static final Map<String, ContextConstructor> registeredContexts = new HashMap<>();

    static {
        registerContext("loot_table", LootTableContext::new);
        registerContext("trading", VillagerTradeContext::new);
        registerContext("death", MobDeathContext::new);
        registerContext("spawn", MobSpawnContext::new);
        registerContext("bartering", BarterContext::new);
        registerContext("fishing", FishingContext::new);
    }
    
    public static void registerContext(String key, ContextConstructor producer) {
        registeredContexts.put(key, producer);
    }

    @Nullable
    public static GenerationContext getContext(String key,
                                               WbsEnchantment enchantment,
                                               ConfigurationSection section,
                                               String directory) {
        ContextConstructor registeredContext = registeredContexts.get(key);
        if (registeredContext == null) {
            return null;
        }

        return registeredContext.from(key, enchantment, section, directory);
    }

    @FunctionalInterface
    private interface ContextConstructor {
        GenerationContext from(String key, WbsEnchantment enchantment, ConfigurationSection section, String directory);
    }
}
