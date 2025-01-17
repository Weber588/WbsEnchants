package wbs.enchants.statuseffects.serialization;

import org.bukkit.persistence.PersistentDataContainer;

public interface PersistentDataContainerSerializable {
    void populate(PersistentDataContainer container);
}
