package wbs.enchants.util;

import net.kyori.adventure.util.Ticks;
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


    /**
     * Starts a new cooldown with this enchantment's key, if off cooldown according to given ticks.
     * @param holder The holder of the cooldown
     * @param cooldownTicks How many ticks must have passed since the cooldown started, to start a new one.
     * @param key The key to use for the cooldown.
     * @return True if a cooldown was started, false if it hasn't been long enough.
     */
    public static boolean newCooldown(PersistentDataHolder holder, int cooldownTicks, NamespacedKey key) {
        if (CooldownManager.getMillisSinceStart(holder, key) >= cooldownTicks * Ticks.SINGLE_TICK_DURATION_MS) {
            CooldownManager.startCooldown(holder, key);
            return true;
        }

        return false;
    }
}
