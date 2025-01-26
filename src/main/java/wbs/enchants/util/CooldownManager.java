package wbs.enchants.util;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantsBootstrap;

public class CooldownManager {
    private static final NamespacedKey KEY = WbsEnchantsBootstrap.createKey("cooldowns");

    public static void startCooldown(PersistentDataHolder holder, NamespacedKey key) {
        PersistentDataContainer fullContainer = holder.getPersistentDataContainer();

        PersistentDataContainer cooldownContainer = fullContainer.get(KEY, PersistentDataType.TAG_CONTAINER);
        if (cooldownContainer == null) {
            cooldownContainer = fullContainer.getAdapterContext().newPersistentDataContainer();
        }

        cooldownContainer.set(key, PersistentDataType.INTEGER, Bukkit.getCurrentTick());

        fullContainer.set(KEY, PersistentDataType.TAG_CONTAINER, cooldownContainer);
    }

    public static int getTimeSinceStart(PersistentDataHolder holder, NamespacedKey key) {
        PersistentDataContainer fullContainer = holder.getPersistentDataContainer();

        PersistentDataContainer cooldownContainer = fullContainer.get(KEY, PersistentDataType.TAG_CONTAINER);

        if (cooldownContainer != null) {
            Integer lastUsedTick = cooldownContainer.get(key, PersistentDataType.INTEGER);

            if (lastUsedTick != null) {
                return Bukkit.getCurrentTick() - lastUsedTick;
            }
        }

        return 0;
    }
}
