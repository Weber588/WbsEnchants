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
import wbs.enchants.enchantment.helper.MovementEnchant;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CavingEnchant extends WbsEnchantment implements MovementEnchant {
    public static final Set<Material> PLACEABLE_TYPES = Set.of(Material.TORCH, Material.SOUL_TORCH);
    public static final int LIGHT_LEVEL_REQUIRED = 0;
    public static final String DESCRIPTION = "Torches inside bundles on your hotbar with this enchantment " +
            "will automatically place themselves on the ground when you walk into the dark!";

    public CavingEnchant() {
        super("caving", DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_BUNDLE);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlock().equals(to.getBlock())) {
            return;
        }

    }

    @Override
    public void onChangeBlock(Player player, Block oldBlock, Block newBlock) {
        if (newBlock.getLightFromBlocks() > LIGHT_LEVEL_REQUIRED) {
            return;
        }
        if (!newBlock.isReplaceable()) {
            return;
        }

        Block onBlock = newBlock.getRelative(BlockFace.DOWN);
        if (!onBlock.getBlockData().isFaceSturdy(BlockFace.UP, BlockSupport.CENTER)) {
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
                    List<ItemStack> bundleItems = new LinkedList<>(bundle.getItems());

                    for (ItemStack bundleItem : bundleItems) {
                        Material type = bundleItem.getType();
                        if (PLACEABLE_TYPES.contains(type)) {
                            if (bundleItem.getAmount() == 1) {
                                bundleItems.remove(bundleItem);
                            } else {
                                bundleItem.setAmount(bundleItem.getAmount() - 1);
                            }

                            newBlock.setType(type);
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
