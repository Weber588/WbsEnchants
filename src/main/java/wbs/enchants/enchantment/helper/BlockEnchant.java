package wbs.enchants.enchantment.helper;

import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.EventUtils;

import java.util.List;

public interface BlockEnchant extends EnchantInterface, AutoRegistrableEnchant {
    NamespacedKey BLOCK_ENCHANTS_KEY = new NamespacedKey(WbsEnchants.getInstance(), "block_enchants");

    static NamespacedKey getBlockKey(Block block) {
        // Don't need to track world; this tag will be stored on a chunk
        String stringKey = block.getX() + "_" + block.getY() + "_" + block.getZ();
        return new NamespacedKey(WbsEnchants.getInstance(), stringKey);
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

        enchantedBlocksContainer.set(blockKey, PersistentDataType.TAG_CONTAINER, container);
        chunkContainer.set(BLOCK_ENCHANTS_KEY, PersistentDataType.TAG_CONTAINER, enchantedBlocksContainer);
    }

    default void registerBlockEvents() {
        EventUtils.register(BlockPlaceEvent.class, this::onPlace);
        EventUtils.register(BlockDropItemEvent.class, this::onDrop);
    }

    default void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return;
        }

        Block block = event.getBlock();
        if (!canEnchant(block)) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        ItemStack placedItem = equipment.getItem(event.getHand());
        if (enchant.containsEnchantment(placedItem)) {
            int level = enchant.getLevel(placedItem);

            PersistentDataContainer blockContainer = getBlockContainer(block);

            blockContainer.set(key, PersistentDataType.INTEGER, level);

            updateBlockContainer(block, blockContainer);

            afterPlace(event, placedItem);
        }
    }

    @Nullable
    default Integer getLevel(Block block) {
        PersistentDataContainer blockContainer = getBlockContainer(block);

        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        return blockContainer.get(key, PersistentDataType.INTEGER);
    }

    default void onDrop(BlockDropItemEvent event) {
        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        PersistentDataContainer blockContainer = getBlockContainer(event.getBlock());

        Integer level = blockContainer.get(key, PersistentDataType.INTEGER);

        blockContainer.remove(key);

        List<Item> items = event.getItems();

        ItemStack dropped = null;
        Material material = event.getBlockState().getBlockData().getPlacementMaterial();
        for (Item item : items) {
            if (item.getItemStack().getType() == material) {
                dropped = item.getItemStack();
                break;
            }
        }

        if (dropped == null) {
            return;
        }

        if (level != null) {
            UberUtils.addEnchantment(enchant, dropped, level);

            afterDrop(event, dropped);
        }
    }

    default void afterPlace(BlockPlaceEvent event, ItemStack placedItem) {

    }

    default void afterDrop(BlockDropItemEvent event, ItemStack droppedItem) {

    }

    boolean canEnchant(Block block);
}
