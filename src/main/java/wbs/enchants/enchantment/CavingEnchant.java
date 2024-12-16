package wbs.enchants.enchantment;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BundleMeta;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

import java.util.List;
import java.util.Set;

public class CavingEnchant extends WbsEnchantment {
    public static final Set<Material> PLACEABLE_TYPES = Set.of(Material.TORCH, Material.SOUL_TORCH);
    public static final int LIGHT_LEVEL_REQUIRED = 2;
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

        if (from.getBlock().equals(to.getBlock())) {
            return;
        }

        if (to.getBlock().getLightFromBlocks() > LIGHT_LEVEL_REQUIRED) {
            return;
        }
        Block block = to.getBlock();
        if (!to.getBlock().isReplaceable()) {
            return;
        }

        Block onBlock = block.getRelative(BlockFace.DOWN);
        if (!onBlock.getBlockData().isFaceSturdy(BlockFace.UP, BlockSupport.CENTER)) {
            return;
        }

        Player player = event.getPlayer();
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
                        Material type = bundleItem.getType();
                        if (PLACEABLE_TYPES.contains(type)) {
                            bundleItem.setAmount(bundleItem.getAmount() - 1);

                            block.setType(type);
                            placedTorch = true;
                            break;
                        }
                    }

                    if (placedTorch) {
                        bundle.setItems(bundleItems);
                        hotbarItem.setItemMeta(bundle);
                    }
                }
            }
        }
    }
}
