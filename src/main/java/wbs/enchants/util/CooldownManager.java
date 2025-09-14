package wbs.enchants.util;

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

        cooldownContainer.set(key, PersistentDataType.LONG, System.currentTimeMillis());

        fullContainer.set(KEY, PersistentDataType.TAG_CONTAINER, cooldownContainer);
    }

    public static long getMillisSinceStart(PersistentDataHolder holder, NamespacedKey key) {
        PersistentDataContainer fullContainer = holder.getPersistentDataContainer();

        PersistentDataContainer cooldownContainer = fullContainer.get(KEY, PersistentDataType.TAG_CONTAINER);

        if (cooldownContainer != null) {
            Long lastUsedMilli = cooldownContainer.get(key, PersistentDataType.LONG);

            if (lastUsedMilli != null) {
                return System.currentTimeMillis() - lastUsedMilli;
            }
        }

        return Long.MAX_VALUE;
    }
}
