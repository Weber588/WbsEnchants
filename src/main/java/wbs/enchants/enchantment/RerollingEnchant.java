package wbs.enchants.enchantment;

import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Location;
import org.bukkit.block.EnchantingTable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.view.EnchantmentView;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;

import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class RerollingEnchant extends WbsEnchantment implements BlockStateEnchant<EnchantingTable> {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Makes the enchanting table re-roll enchantments when you close the inventory.";

    public RerollingEnchant() {
        super("rerolling", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.ENCHANTING_TABLE)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_ENCHANTING_TABLE);
    }

    @Override
    public Class<EnchantingTable> getStateClass() {
        return EnchantingTable.class;
    }

    @EventHandler
    public void onOpenEnchantingTable(InventoryOpenEvent event) {
        if (!(event.getView() instanceof EnchantmentView view)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof TileStateInventoryHolder holder)) {
            return;
        }

        if (!isEnchanted(holder.getBlock())) {
            return;
        }

        view.setEnchantmentSeed(new Random().nextInt());
    }

    @EventHandler
    public void onClickEnchantSlot(InventoryClickEvent event) {
        if (event.getSlot() != 0) {
            return;
        }

        if (event.getClickedInventory() == null || !(event.getClickedInventory() instanceof EnchantingInventory enchantingInventory)) {
            return;
        }

        Location location = enchantingInventory.getLocation();
        if (location == null || !isEnchanted(location.getBlock())) {
            return;
        }

        if (event.getView() instanceof EnchantmentView view) {
            view.setEnchantmentSeed(new Random().nextInt());
        }
    }
}
