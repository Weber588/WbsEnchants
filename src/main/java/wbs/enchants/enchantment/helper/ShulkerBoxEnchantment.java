package wbs.enchants.enchantment.helper;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemContainerContents;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.util.PersistentInventoryDataType;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public interface ShulkerBoxEnchantment extends BlockEnchant {
    NamespacedKey EXTRA_INVENTORY_KEY = WbsEnchantsBootstrap.createKey("extra_inventory");

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

        return new ShulkerBoxWrapper(box, item, Math.max(0, EnchantManager.CARRYING.getLevel(item) - 1));
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

    @SuppressWarnings("UnstableApiUsage")
    class ShulkerBoxWrapper extends ContainerItemWrapper implements InventoryHolder {
        private final @NotNull ShulkerBox box;
        private final @NotNull Inventory inventory;

        public ShulkerBoxWrapper(@NotNull ShulkerBox box, @NotNull ItemStack item, @Range(from = 0, to = 3) int extraRows) {
            super(item);
            this.box = box;

            extraRows = Math.clamp(extraRows, 0, 3);

            Inventory baseInventory = box.getInventory();

            PersistentDataContainer container = box.getPersistentDataContainer();
            Inventory extraInventory = container.get(EXTRA_INVENTORY_KEY, PersistentInventoryDataType.INSTANCE);

            inventory = Bukkit.createInventory(this, 9 * (3 + extraRows), displayName());
            for (int i = 0; i < baseInventory.getSize(); i++) {
                inventory.setItem(i, baseInventory.getItem(i));
            }

            if (extraInventory != null) {
                for (int i = 0; i < extraInventory.getSize(); i++) {
                    inventory.setItem(baseInventory.getSize() + i, extraInventory.getItem(i));
                }
            }
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

        @Override
        public void saveToItem() {
            Inventory baseInventory = box.getInventory();

            for (int i = 0; i < baseInventory.getSize(); i++) {
                baseInventory.setItem(i, inventory.getItem(i));
            }

            if (inventory.getSize() > baseInventory.getSize()) {
                Inventory extraInventory = Bukkit.createInventory(this, inventory.getSize() - baseInventory.getSize(), displayName());

                for (int i = 0; i < inventory.getSize() - baseInventory.getSize(); i++) {
                    extraInventory.setItem(i, inventory.getItem(i + baseInventory.getSize()));
                }

                box.getPersistentDataContainer().set(EXTRA_INVENTORY_KEY, PersistentInventoryDataType.INSTANCE, extraInventory);
            }

            ItemMeta meta = item().getItemMeta();

            if (meta == null) {
                return;
            }

            if (!(meta instanceof BlockStateMeta blockStateMeta)) {
                return;
            }

            blockStateMeta.setBlockState(box);

            item().setItemMeta(blockStateMeta);

            ItemContainerContents.Builder builder = ItemContainerContents.containerContents();
            inventory.forEach(item -> builder.add(Objects.requireNonNullElseGet(item, ItemStack::empty)));
            item().setData(DataComponentTypes.CONTAINER, builder.build());
        }

        @Override
        public boolean containsAtLeast(ItemStack item, int amountRequired) {
            return inventory.containsAtLeast(item, amountRequired);
        }

        @Override
        public ItemStack addItem(ItemStack other) {
            return inventory.addItem(other).get(0);
        }

        @Override
        public ItemStack[] getItems() {
            return inventory.getContents();
        }

        @Override
        public void removeItem(ItemStack stack) {
            inventory.removeItem(stack);
        }
    }
}
