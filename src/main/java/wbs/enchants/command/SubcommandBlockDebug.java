package wbs.enchants.command;

import com.mojang.brigadier.Command;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.util.PersistentInventoryDataType;
import wbs.utils.util.plugin.WbsPlugin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SubcommandBlockDebug extends AbstractBlockSubcommand {
    public SubcommandBlockDebug(@NotNull WbsPlugin plugin) {
        super(plugin, "debug");
    }

    @Override
    public int executeOnBlock(CommandSender sender, Block block) {
        plugin.sendMessage("Chunk persistent data dump: " +
                toString(block.getChunk().getPersistentDataContainer()), sender);

        if (block.getState() instanceof TileState state) {
            plugin.sendMessage("Block persistent data dump: " +
                    toString(state.getPersistentDataContainer()), sender);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static final Set<PersistentDataType<?, ?>> KNOWN_DATA_TYPES = Set.of(
            PersistentInventoryDataType.INSTANCE,
            PersistentDataType.TAG_CONTAINER,
            PersistentDataType.LONG_ARRAY,
            PersistentDataType.INTEGER_ARRAY,
            PersistentDataType.BYTE_ARRAY,
            PersistentDataType.STRING,
            PersistentDataType.BOOLEAN,
            PersistentDataType.DOUBLE,
            PersistentDataType.FLOAT,
            PersistentDataType.LONG,
            PersistentDataType.INTEGER,
            PersistentDataType.SHORT,
            PersistentDataType.BYTE,
            PersistentDataType.LIST.dataContainers(),
            PersistentDataType.LIST.longArrays(),
            PersistentDataType.LIST.integerArrays(),
            PersistentDataType.LIST.byteArrays(),
            PersistentDataType.LIST.strings(),
            PersistentDataType.LIST.booleans(),
            PersistentDataType.LIST.doubles(),
            PersistentDataType.LIST.floats(),
            PersistentDataType.LIST.longs(),
            PersistentDataType.LIST.integers(),
            PersistentDataType.LIST.shorts(),
            PersistentDataType.LIST.bytes()
    );

    private String toString(PersistentDataContainer container) {
        StringBuilder asString = new StringBuilder("{");

        for (NamespacedKey key : container.getKeys()) {
            boolean found = false;
            for (PersistentDataType<?, ?> knownDataType : KNOWN_DATA_TYPES) {
                try {
                    asString.append(getDataString(container, knownDataType, key)).append("\n");
                    found = true;
                    break;
                } catch (IllegalArgumentException ignored) {}
            }

            if (!found) {
                asString.append(key.asString()).append(": <UNPARSEABLE>\n");
            }
        }

        return asString + "}";
    }

    private <P, C> String getDataString(PersistentDataContainer container, PersistentDataType<P, C> knownDataType, NamespacedKey key) throws IllegalArgumentException {
        C object = container.get(key, knownDataType);

        return key.asString() + ": " + parseKnownDataTypes(object);
    }

    private String parseKnownDataTypes(Object object) {
        if (object instanceof Inventory inventory) {
            return "[custom inventory]";
        } else if (object instanceof PersistentDataContainer nestedContainer) {
            return toString(nestedContainer);
        } else if (object instanceof List<?> list) {
            String asString = "[";

            asString += list.stream().map(this::parseKnownDataTypes).collect(Collectors.joining(",\n "));

            return asString + "\n]";
        }

        return object.toString();
    }
}
