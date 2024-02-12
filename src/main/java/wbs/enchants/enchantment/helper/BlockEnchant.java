package wbs.enchants.enchantment.helper;

import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
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
import wbs.enchants.util.EventUtils;

import java.util.List;

public interface BlockEnchant extends EnchantInterface {
    default void registerBlockEvents() {
        EventUtils.register(BlockPlaceEvent.class, this::onPlace);
        EventUtils.register(BlockDropItemEvent.class, this::onDrop);
    }

    default void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof TileState state)) {
            return;
        }

        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return;
        }

        if (!canEnchant(state)) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        ItemStack placedItem = equipment.getItem(event.getHand());
        if (enchant.containsEnchantment(placedItem)) {
            int level = enchant.getLevel(placedItem);

            PersistentDataContainer container = state.getPersistentDataContainer();
            container.set(key, PersistentDataType.INTEGER, level);

            state.update();

            afterPlace(event, placedItem);
        }
    }

    default Integer getLevel(Block block) {
        return getLevel(block.getState());
    }

    @Nullable
    default Integer getLevel(BlockState state) {
        if (!(state instanceof TileState tileState)) {
            return null;
        }
        PersistentDataContainer entityContainer = tileState.getPersistentDataContainer();
        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        return entityContainer.get(key, PersistentDataType.INTEGER);
    }

    default void onDrop(BlockDropItemEvent event) {
        if (!(event.getBlockState() instanceof TileState state)) {
            return;
        }

        List<Item> items = event.getItems();

        ItemStack dropped = null;
        Material material = state.getBlockData().getPlacementMaterial();
        for (Item item : items) {
            if (item.getItemStack().getType() == material) {
                dropped = item.getItemStack();
                break;
            }
        }

        if (dropped == null) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();
        NamespacedKey key = enchant.getKey();

        PersistentDataContainer entityContainer = state.getPersistentDataContainer();

        Integer level = entityContainer.get(key, PersistentDataType.INTEGER);
        if (level != null) {
            UberUtils.addEnchantment(enchant, dropped, level);

            afterDrop(event, dropped);
        }
    }

    default void afterPlace(BlockPlaceEvent event, ItemStack placedItem) {

    }

    default void afterDrop(BlockDropItemEvent event, ItemStack droppedItem) {

    }

    boolean canEnchant(TileState state);
}
