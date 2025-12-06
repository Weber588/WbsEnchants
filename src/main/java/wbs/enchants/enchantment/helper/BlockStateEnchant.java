package wbs.enchants.enchantment.helper;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface BlockStateEnchant<T extends BlockState> extends BlockEnchant {
    Class<T> getStateClass();

    @Override
    default boolean canEnchant(Block block) {
        return getStateClass().isInstance(block.getState());
    }

    @Override
    default void afterPlace(BlockPlaceEvent event, ItemStack placedItem) {
        BlockState state = event.getBlock().getState();

        if (getStateClass().isInstance(state)) {
            editStateOnPlace(event, getStateClass().cast(state), placedItem);
        }

        if (state.isPlaced()) {
            state.update();
        }
    }

    @Override
    default void afterDrop(@NotNull BlockState state, ItemStack droppedItem) {
        if (getStateClass().isInstance(state)) {
            afterDrop(state, getStateClass().cast(state), droppedItem);
        }

        if (state.isPlaced()) {
            state.update();
        }
    }

    default boolean isEnchanted(T t) {
        return BlockEnchant.super.isEnchanted(t.getBlock());
    }

    default void editStateOnPlace(BlockPlaceEvent event, T t, ItemStack placedItem) {

    }

    default void afterDrop(@NotNull BlockState state, T t, ItemStack droppedItem) {

    }
}
