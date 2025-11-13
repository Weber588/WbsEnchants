package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockSupport;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BundleMeta;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.MovementEnchant;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CavingEnchant extends WbsEnchantment implements MovementEnchant {
    public static final Set<Material> PLACEABLE_TYPES = Set.of(Material.TORCH, Material.SOUL_TORCH);
    public static final int DEFAULT_LIGHT_LEVEL_REQUIRED = 3;
    public static final String DESCRIPTION = "Torches inside bundles on your hotbar with this enchantment " +
            "will automatically place themselves on the ground when you walk into the dark!";

    private int lightLevelRequired = DEFAULT_LIGHT_LEVEL_REQUIRED;

    public CavingEnchant() {
        super("caving", DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(ItemTypeKeys.BUNDLE)
                .minimumCost(5, 6)
                .maximumCost(25, 6);
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        super.configure(section, directory);

        lightLevelRequired = section.getInt("light-level-required", DEFAULT_LIGHT_LEVEL_REQUIRED);
    }

    @Override
    public void onChangeBlock(Player player, Block oldBlock, Block newBlock) {
        // TODO: Make it configurable if this only works when sky light = 0 (i.e. only caves/indoors), or if it works outside
        if (newBlock.getLightFromBlocks() > lightLevelRequired) {
            return;
        }
        if (!newBlock.isReplaceable()) {
            return;
        }
        if (player.isInWater()) {
            return;
        }

        Block onBlock = newBlock.getRelative(BlockFace.DOWN);
        if (!onBlock.getBlockData().isFaceSturdy(BlockFace.UP, BlockSupport.CENTER)) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        boolean placedTorch = false;
        // TODO: Make it configurable if it needs to be in the hotbar, a hand, or anywhere in the inv
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
