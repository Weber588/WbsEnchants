package wbs.enchants.enchantment.helper;

import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public interface ShulkerBoxEnchantment extends BlockEnchant {
    @Override
    default boolean canEnchant(Block block) {
        return block.getState() instanceof ShulkerBox;
    }

    @Nullable
    @Contract("null -> null")
    default ShulkerBoxWrapper getShulkerBox(ItemStack item) {
        if (item == null) {
            return null;
        }

        if (!Tag.SHULKER_BOXES.isTagged(item.getType())) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return null;
        }

        if (!(meta instanceof BlockStateMeta blockStateMeta)) {
            return null;
        }

        if (!(blockStateMeta.getBlockState() instanceof ShulkerBox box)) {
            return null;
        }

        return new ShulkerBoxWrapper(box, item);
    }

    default List<ShulkerBoxWrapper> getEnchantedBoxes(Player player) {
        List<ShulkerBoxWrapper> enchantedBoxes = new LinkedList<>();
        for (ItemStack item : player.getInventory()) {
            if (item != null && getThisEnchantment().isEnchantmentOn(item)) {
                ShulkerBoxWrapper wrapper = getShulkerBox(item);
                if (wrapper != null) {
                    enchantedBoxes.add(wrapper);
                }
            }
        }

        return enchantedBoxes;
    }

    class ShulkerBoxWrapper extends ContainerItemWrapper {
        private final @NotNull ShulkerBox box;

        public ShulkerBoxWrapper(@NotNull ShulkerBox box, @NotNull ItemStack item) {
            super(item);
            this.box = box;
        }

        @Override
        public void saveToItem() {
            ItemMeta meta = item().getItemMeta();

            if (meta == null) {
                return;
            }

            if (!(meta instanceof BlockStateMeta blockStateMeta)) {
                return;
            }

            blockStateMeta.setBlockState(box);

            item().setItemMeta(blockStateMeta);
        }

        @Override
        public boolean containsAtLeast(ItemStack item, int amountRequired) {
            return box.getInventory().containsAtLeast(item, amountRequired);
        }

        @Override
        public ItemStack addItem(ItemStack other) {
            return box.getInventory().addItem(other).get(0);
        }

        @Override
        public ItemStack[] getItems() {
            return box.getInventory().getContents();
        }

        @Override
        public void removeItem(ItemStack stack) {
            box.getInventory().removeItem(stack);
        }

        public @NotNull ShulkerBox box() {
            return box;
        }
    }
}
