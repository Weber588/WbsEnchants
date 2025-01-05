package wbs.enchants.enchantment;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.enchants.util.EntityUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HandheldEnchant extends WbsEnchantment implements BlockEnchant {
    private static final String DEFAULT_DESCRIPTION = "An enchantment for a variety of utility blocks that allow " +
            "them to be used directly from your hand, no placing required!";

    // Making this public so other enchants/plugins can theoretically add their own functionality if needed
    public final Map<Material, RightClickFunction> functionalities = new HashMap<>();

    public HandheldEnchant() {
        super("handheld", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_HANDHELD);

        loadFunctionalities();
    }

    // TODO: Base this on supportedItems tag but for blocks?... How does that work with blocktype vs itemtype?
    @Override
    public boolean canEnchant(Block block) {
        return functionalities.containsKey(block.getType());
    }

    private void loadFunctionalities() {
        functionalities.put(Material.CRAFTING_TABLE, (event, player, item) ->
                player.openWorkbench(null, true));
        functionalities.put(Material.ENCHANTING_TABLE, (event, player, item) ->
                player.openEnchanting(null, true));
        functionalities.put(Material.ENDER_CHEST, (event, player, item) ->
                player.openInventory(player.getEnderChest()));

        functionalities.put(Material.CARTOGRAPHY_TABLE, (InventoryOpenFunction) () -> InventoryType.CARTOGRAPHY);
        functionalities.put(Material.STONECUTTER, (InventoryOpenFunction) () -> InventoryType.STONECUTTER);
        functionalities.put(Material.GRINDSTONE, (InventoryOpenFunction) () -> InventoryType.GRINDSTONE);
        functionalities.put(Material.LOOM, (InventoryOpenFunction) () -> InventoryType.LOOM);
        functionalities.put(Material.SMITHING_TABLE, (InventoryOpenFunction) () -> InventoryType.SMITHING);

        // TODO: Add shulker box to allow opening a shulker box directly from inventory?
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isSneaking()) {
            return;
        }

        ItemStack item = EntityUtils.getEnchantedFromSlot(event.getPlayer(), this, event.getHand());

        if (item != null) {
            RightClickFunction function = functionalities.get(item.getType());

            if (function == null) {
                sendActionBar("&wThere is no functionality defined for this block.", player);
                return;
            }

            function.onRightClick(event, player, item);
            event.setCancelled(true);
        }
    }

    @FunctionalInterface
    public interface RightClickFunction {
        void onRightClick(PlayerInteractEvent event, Player player, ItemStack item);
    }

    @FunctionalInterface
    private interface InventoryOpenFunction extends RightClickFunction {
        InventoryType getInventoryType();

        @Override
        default void onRightClick(PlayerInteractEvent event, Player player, ItemStack item) {
            ItemMeta meta = item.getItemMeta();
            player.openInventory(
                    Bukkit.createInventory(
                            player,
                            getInventoryType(),
                            Objects.requireNonNullElse(
                                    meta.displayName(),
                                    meta.itemName()
                            )
                    )
            );
        }
    }
}
