package wbs.enchants.statuseffects.serialization;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PersistentDataContainerSerializer<T extends PersistentDataContainerSerializable> implements PersistentDataType<PersistentDataContainer, T> {
    @NotNull
    private final Class<T> complexClass;
    @NotNull
    private final Deserializer<T> deserializer;

    public PersistentDataContainerSerializer(@NotNull Class<T> complexClass, @NotNull Deserializer<T> deserializer) {
        this.complexClass = complexClass;
        this.deserializer = deserializer;
    }

    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    public @NotNull PersistentDataContainer toPrimitive(@NotNull T t, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        PersistentDataContainer container = persistentDataAdapterContext.newPersistentDataContainer();

        t.populate(container);

        return container;
    }

    public @NotNull T fromPrimitive(@NotNull PersistentDataContainer persistentDataContainer, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        return deserializer.deserialize(persistentDataContainer, persistentDataAdapterContext);
    }

    public @NotNull T fromPrimitive(@NotNull PersistentDataContainer persistentDataContainer) {
        return deserializer.deserialize(persistentDataContainer, persistentDataContainer.getAdapterContext());
    }

    @Override
    @NotNull
    public Class<T> getComplexType() {
        return complexClass;
    }

    public interface Deserializer<T> {
        T deserialize(@NotNull PersistentDataContainer persistentDataContainer, @NotNull PersistentDataAdapterContext persistentDataAdapterContext);
    }
}
