package wbs.enchants.enchantment.shulkerbox;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ShulkerBoxEnchantment;
import wbs.utils.util.WbsEventUtils;

public class CarryingEnchant extends WbsEnchantment implements ShulkerBoxEnchantment {
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

        player.openInventory(wrapper.getInventory());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();

        if (!(topInventory.getHolder() instanceof ShulkerBoxWrapper holder)) {
            return;
        }

        ItemStack addedItem = WbsEventUtils.getItemAddedToTopInventory(event);
        if (!holder.canContain(addedItem)) {
            event.setCancelled(true);
        } else {
            holder.saveToItem();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof ShulkerBoxWrapper holder)) {
            return;
        }

        holder.saveToItem();
    }
}
