package wbs.enchants.enchantment;

import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.event.inventory.HopperInventorySearchEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.BlockEnchant;

import java.util.HashMap;
import java.util.Map;

public class EncryptedEnchant extends WbsEnchantment implements BlockEnchant {
    private static final Map<String, EncryptedInventoryHolder> HOLDERS = new HashMap<>();
    // TODO: Serialize and load the holders in a separate config file

    private static final String DEFAULT_DESCRIPTION = "";

    public EncryptedEnchant() {
        super("encrypted", DEFAULT_DESCRIPTION);
    }

    @Override
    public boolean canEnchant(Block block) {
        return (block.getState() instanceof EnderChest);
    }

    public void onHopperFindInventory(HopperInventorySearchEvent event) {
        // Don't check container type -- we want it to work for both source and destination
        Block block = event.getBlock();
        if (getLevel(block) != null) {

        }
    }

    private static class EncryptedInventoryHolder implements InventoryHolder {
        private final String key;
        private final Inventory inventory;

        private EncryptedInventoryHolder(String key, Inventory inventory) {
            this.key = key;
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }
}
