package wbs.enchants.enchantment.shulkerbox;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ShulkerBoxEnchantment;
import wbs.enchants.util.PersistentInventoryDataType;
import wbs.utils.util.WbsEventUtils;

public class CarryingEnchant extends WbsEnchantment implements ShulkerBoxEnchantment {
    private static final NamespacedKey EXTRA_INVENTORY_KEY = WbsEnchantsBootstrap.createKey("extra_inventory");

    private static final String DEFAULT_DESCRIPTION = "Allows you to open the shulker box from your hand by right clicking," +
            "and gains an extra 9 inventory slots per level above the first.";

    public CarryingEnchant() {
        super("carrying", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_SHULKER_BOX)
                .maxLevel(4);
    }

    // Interact event should prevent this one from even firing, but doing this for safety --
    // it won't work on the ground without a lot more risk of dupe bugs due to fake inventory manipulation.
    // Doing it in both LOWEST priority (so we get the first say to cancel it), AND the HIGHEST priority
    // so we also get the final say in case something is wild and does it anyway.
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlaceLowest(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (isEnchantmentOn(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceHighest(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (isEnchantmentOn(item)) {
            event.setCancelled(true);
        }
    }

    // Prevent shulker being placed via dispenser
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();

        if (isEnchantmentOn(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClick(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack item = event.getItem();

        if (item == null || item.isEmpty()) {
            return;
        }

        if (!isEnchantmentOn(item)) {
            return;
        }

        ShulkerBoxWrapper wrapper = getShulkerBox(item);

        if (wrapper == null) {
            return;
        }

        event.setCancelled(true);

        ShulkerBoxHolder holder = new ShulkerBoxHolder(wrapper, getLevel(item) - 1);

        player.openInventory(holder.getInventory());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();

        if (!(topInventory.getHolder() instanceof ShulkerBoxHolder holder)) {
            return;
        }

        ItemStack addedItem = WbsEventUtils.getItemAddedToTopInventory(event);
        if (!holder.box.canContain(addedItem)) {
            event.setCancelled(true);
        } else {
            holder.save();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof ShulkerBoxHolder holder)) {
            return;
        }

        holder.save();
    }

    private static class ShulkerBoxHolder implements InventoryHolder {
        private final @NotNull Inventory inventory;
        private final ShulkerBoxWrapper box;

        private ShulkerBoxHolder(ShulkerBoxWrapper wrapper, int extraRows) {
            this.box = wrapper;

            Inventory baseInventory = wrapper.box().getInventory();

            PersistentDataContainer container = wrapper.box().getPersistentDataContainer();
            Inventory extraInventory = container.get(EXTRA_INVENTORY_KEY, PersistentInventoryDataType.INSTANCE);

            inventory = Bukkit.createInventory(this, 9 * (3 + extraRows), wrapper.displayName());
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

        public boolean canContain(ItemStack check) {
            return box.canContain(check);
        }

        public void save() {
            Inventory baseInventory = box.box().getInventory();

            for (int i = 0; i < baseInventory.getSize(); i++) {
                baseInventory.setItem(i, inventory.getItem(i));
            }

            if (inventory.getSize() > baseInventory.getSize()) {
                Inventory extraInventory = Bukkit.createInventory(this, inventory.getSize() - baseInventory.getSize(), box.displayName());

                for (int i = 0; i < inventory.getSize() - baseInventory.getSize(); i++) {
                    extraInventory.setItem(i, inventory.getItem(i + baseInventory.getSize()));
                }

                box.box().getPersistentDataContainer().set(EXTRA_INVENTORY_KEY, PersistentInventoryDataType.INSTANCE, extraInventory);
            }


            box.saveToItem();
        }
    }
}
