package wbs.enchants.enchantment.helper;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.events.EnchantedBlockBreakEvent;
import wbs.enchants.events.EnchantedBlockPlaceEvent;
import wbs.enchants.util.EnchantUtils;
import wbs.enchants.util.EventUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an enchantment that can go on a block in the inventory, that will persist in the chunk data, allowing it
 * to be placed and picked up while retaining the enchantment.
 */
public interface BlockEnchant extends EnchantInterface, AutoRegistrableEnchant {
    NamespacedKey BLOCK_ENCHANTS_KEY = new NamespacedKey("wbsenchants", "block_enchants");

    static NamespacedKey getBlockKey(Block block) {
        // Don't need to track world; this tag will be stored on a chunk
        String stringKey = block.getX() + "_" + block.getY() + "_" + block.getZ();
        return new NamespacedKey(WbsEnchants.getInstance(), stringKey);
    }

    static Block getBlock(NamespacedKey key, World world) {
        String stringPosition = key.value();

        String[] components = stringPosition.split("_");

        return world.getBlockAt(Integer.parseInt(components[0]), Integer.parseInt(components[1]), Integer.parseInt(components[2]));
    }

    static PersistentDataContainer getBlockContainer(Block block) {
        PersistentDataContainer chunkContainer = block.getChunk().getPersistentDataContainer();

        PersistentDataContainer enchantedBlocksContainer = chunkContainer.get(BLOCK_ENCHANTS_KEY, PersistentDataType.TAG_CONTAINER);

        if (enchantedBlocksContainer == null) {
            enchantedBlocksContainer = chunkContainer.getAdapterContext().newPersistentDataContainer();
        }

        NamespacedKey blockKey = getBlockKey(block);
        PersistentDataContainer blockContainer = enchantedBlocksContainer.get(blockKey, PersistentDataType.TAG_CONTAINER);

        if (blockContainer == null) {
            blockContainer = enchantedBlocksContainer.getAdapterContext().newPersistentDataContainer();
        }

        return blockContainer;
    }

    static void updateBlockContainer(Block block, PersistentDataContainer container) {
        PersistentDataContainer chunkContainer = block.getChunk().getPersistentDataContainer();

        PersistentDataContainer enchantedBlocksContainer = chunkContainer.get(BLOCK_ENCHANTS_KEY, PersistentDataType.TAG_CONTAINER);

        if (enchantedBlocksContainer == null) {
            enchantedBlocksContainer = chunkContainer.getAdapterContext().newPersistentDataContainer();
        }

        NamespacedKey blockKey = getBlockKey(block);

        if (container == null || container.isEmpty()) {
            enchantedBlocksContainer.remove(blockKey);
        } else {
            enchantedBlocksContainer.set(blockKey, PersistentDataType.TAG_CONTAINER, container);
        }
        chunkContainer.set(BLOCK_ENCHANTS_KEY, PersistentDataType.TAG_CONTAINER, enchantedBlocksContainer);
    }

    static Map<Block, Map<BlockEnchant, Integer>> getBlockEnchantments(Chunk chunk) {
        PersistentDataContainer chunkContainer = chunk.getPersistentDataContainer();

        PersistentDataContainer enchantedBlocksContainer = chunkContainer.get(BLOCK_ENCHANTS_KEY, PersistentDataType.TAG_CONTAINER);

        Map<Block, Map<BlockEnchant, Integer>> enchantedBlocks = new HashMap<>();

        if (enchantedBlocksContainer == null) {
            return enchantedBlocks;
        }

        enchantedBlocksContainer.getKeys().forEach(blockKey -> {
            PersistentDataContainer blockEnchantsContainer = enchantedBlocksContainer.get(blockKey, PersistentDataType.TAG_CONTAINER);

            if (blockEnchantsContainer == null) {
                return;
            }

            Block block = getBlock(blockKey, chunk.getWorld());

            enchantedBlocks.put(block, getEnchantments(block));
        });

        return enchantedBlocks;
    }

    private static @Nullable Integer getLevel(WbsEnchantment enchant, PersistentDataContainer blockContainer) {
        NamespacedKey key = enchant.getKey();

        return blockContainer.get(key, PersistentDataType.INTEGER);
    }

    static Map<BlockEnchant, Integer> getEnchantments(Block block) {
        PersistentDataContainer blockContainer = getBlockContainer(block);

        return getEnchantments(blockContainer);
    }

    static Map<BlockEnchant, Integer> getEnchantments(PersistentDataContainer blockContainer) {
        Map<BlockEnchant, Integer> enchantments = new HashMap<>();
        for (NamespacedKey key : blockContainer.getKeys()) {
            WbsEnchantment enchantment = EnchantManager.getCustomFromKey(key);
            if (enchantment instanceof BlockEnchant blockEnchant) {
                enchantments.put(blockEnchant, blockContainer.get(key, PersistentDataType.INTEGER));
            }
        }

        return enchantments;
    }

    static boolean isEnchanted(Block block) {
        PersistentDataContainer blockContainer = getBlockContainer(block);

        return !blockContainer.isEmpty();
    }

    default void registerBlockEvents() {
        EventUtils.register(BlockPlaceEvent.class, this::onPlace, EventPriority.NORMAL, true);
        EventUtils.register(BlockDropItemEvent.class, this::onDrop, EventPriority.NORMAL, true);
        EventUtils.register(ChunkLoadEvent.class, this::onLoad, EventPriority.NORMAL, true);
    }

    default void onLoad(ChunkLoadEvent event) {
        Map<Block, Map<BlockEnchant, Integer>> blockEnchantments = getBlockEnchantments(event.getChunk());

        blockEnchantments.forEach((block, enchants) -> {
            if (enchants.containsKey(this)) {
                onLoad(event, block, enchants.get(this));
            }
        });
    }

    default void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();

        Block block = event.getBlock();
        if (!canEnchant(block)) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        ItemStack placedItem = equipment.getItem(event.getHand());
        if (enchant.isEnchantmentOn(placedItem)) {
            int level = enchant.getLevel(placedItem);

            PersistentDataContainer blockContainer = getBlockContainer(block);

            blockContainer.set(key, PersistentDataType.INTEGER, level);

            updateBlockContainer(block, blockContainer);

            EnchantedBlockPlaceEvent enchantedEvent = new EnchantedBlockPlaceEvent(this, block);
            Bukkit.getPluginManager().callEvent(enchantedEvent);

            afterPlace(event, placedItem);
        }
    }

    @Nullable
    default Integer getLevel(Block block) {
        PersistentDataContainer blockContainer = getBlockContainer(block);

        WbsEnchantment enchant = getThisEnchantment();
        return getLevel(enchant, blockContainer);
    }

    default void onDrop(BlockDropItemEvent event) {
        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        Block block = event.getBlock();
        PersistentDataContainer blockContainer = getBlockContainer(block);

        Integer level = blockContainer.get(key, PersistentDataType.INTEGER);

        blockContainer.remove(key);
        updateBlockContainer(block, blockContainer);

        List<Item> items = event.getItems();

        ItemStack dropped = null;
        Material material = event.getBlockState().getBlockData().getPlacementMaterial();
        for (Item item : items) {
            if (item.getItemStack().getType() == material && item.getItemStack().getAmount() == 1) {
                dropped = item.getItemStack();
                break;
            }
        }

        if (dropped == null) {
            return;
        }

        if (level != null) {
            EnchantedBlockBreakEvent enchantedEvent = new EnchantedBlockBreakEvent(this, block);
            Bukkit.getPluginManager().callEvent(enchantedEvent);

            EnchantUtils.addEnchantment(enchant, dropped, level);

            afterDrop(event, dropped);
        }
    }

    default void afterPlace(BlockPlaceEvent event, ItemStack placedItem) {

    }

    default void afterDrop(BlockDropItemEvent event, ItemStack droppedItem) {

    }

    default void onLoad(ChunkLoadEvent event, Block block, int level) {

    }

    boolean canEnchant(Block block);
}
