package wbs.enchants.enchantment;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BundleMeta;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

import java.util.List;

public class CavingEnchant extends WbsEnchantment {
    public static final String DESCRIPTION = "Torches inside bundles on your hotbar with this enchantment " +
            "will automatically place themselves on the ground when you walk into the dark!";

    public CavingEnchant() {
        super("caving", DESCRIPTION);

        maxLevel = 1;
        supportedItems = WbsEnchantsBootstrap.ENCHANTABLE_BUNDLE;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Caving";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.toBlockLocation().equals(to.toBlockLocation())) {
            return;
        }

        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();
        if (!block.isBuildable()) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        boolean placedTorch = false;
        for (int i = 0; i < 9; i++) {
            if (placedTorch) {
                break;
            }
            ItemStack hotbarItem = inventory.getItem(i);

            if (hotbarItem == null) {
                continue;
            }

            if (hotbarItem.getItemMeta() instanceof BundleMeta bundle) {
                if (isEnchantmentOn(hotbarItem)) {
                    List<ItemStack> bundleItems = bundle.getItems();

                    for (ItemStack bundleItem : bundleItems) {
                        // TODO: Add support for other types (Lanterns?)
                        if (bundleItem.getType() == Material.TORCH) {
                            bundleItem.setAmount(bundleItem.getAmount() - 1);

                            block.setType(bundleItem.getType());
                            placedTorch = true;
                            break;
                        }
                    }
                }
            }
        }
    }
}
