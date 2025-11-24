package wbs.enchants.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.serializer.SerializerException;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.WbsEnums;

import java.util.Map;
import java.util.UUID;

public class PersistentInventoryDataType implements PersistentDataType<String, Inventory> {
    public static PersistentInventoryDataType INSTANCE = new PersistentInventoryDataType();

    private static final String SERIALIZATION_KEY_INVENTORY = "inventory";
    private static final String SERIALIZATION_KEY_TYPE = "type";
    private static final String SERIALIZATION_KEY_SIZE = "size";

    @NotNull
    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @NotNull
    @Override
    public Class<Inventory> getComplexType() {
        return Inventory.class;
    }

    @NotNull
    @Override
    public String toPrimitive(@NotNull Inventory inventory, @Nullable PersistentDataAdapterContext persistentDataAdapterContext) {
        YamlConfiguration tempConfig = new YamlConfiguration();

        ConfigurationSection section = tempConfig.createSection(SERIALIZATION_KEY_INVENTORY);

        section.set(SERIALIZATION_KEY_TYPE, inventory.getType().name());
        section.set(SERIALIZATION_KEY_SIZE, inventory.getSize());

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack check = inventory.getItem(i);
            if (check != null) {
                section.set(String.valueOf(i), check.serialize());
            }
        }

        return tempConfig.saveToString();
    }

    @NotNull
    @Override
    public Inventory fromPrimitive(@NotNull String inventoryString, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        YamlConfiguration tempConfig = new YamlConfiguration();

        try {
            tempConfig.loadFromString(inventoryString);
        } catch (InvalidConfigurationException e) {
            WbsEnchants.getInstance().getLogger().info("Inventory failed to load from config: " + inventoryString);
            e.printStackTrace();
            throw new SerializerException(e.getMessage());
        }

        ConfigurationSection asSection = tempConfig.getConfigurationSection(SERIALIZATION_KEY_INVENTORY);
        if (asSection == null) {
            throw new SerializerException("Failed to convert item to serialised config section.");
        }

        InventoryType type = InventoryType.CHEST;
        String typeString = asSection.getString(SERIALIZATION_KEY_TYPE);
        if (typeString != null) {
            type = WbsEnums.getEnumFromString(InventoryType.class, typeString);
            if (type == null) {
                type = InventoryType.CHEST;
            }
        }

        int size = asSection.getInt(SERIALIZATION_KEY_SIZE);

        Inventory inventory;
        if ((type == InventoryType.CHEST || type == InventoryType.PLAYER) && size / 9 <= 6 && size / 9 >= 1 ) {
            inventory = Bukkit.getServer().createInventory(null, size, UUID.randomUUID().toString());
        } else {
            inventory = Bukkit.getServer().createInventory(null, type, UUID.randomUUID().toString());
        }


        for (int i = 0; i < inventory.getSize(); i++) {
            ConfigurationSection slotSection = asSection.getConfigurationSection(String.valueOf(i));
            if (slotSection != null) {
                Map<String, Object> asMap = slotSection.getValues(false);
                ItemStack deserialized = ItemStack.deserialize(asMap);
                inventory.setItem(i, deserialized);
            }
        }

        return inventory;
    }
}
