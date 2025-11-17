package wbs.enchants.enchantment.shulkerbox;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ShulkerBoxEnchantment;
import wbs.enchants.util.BlockUtils;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.entities.WbsEntityUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class PlacingEnchant extends WbsEnchantment implements ShulkerBoxEnchantment {
    private static final String DEFAULT_DESCRIPTION = "Placing the shulker box will instead place a random item from " +
            "inside, if any are available. Sneak to place actual shulker box.";

    public PlacingEnchant() {
        super("placing", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_SHULKER_BOX)
                .exclusiveWith(EnchantManager.CARRYING);
    }

    // Cancel before it gets to BlockEnchant events if needed
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFirstPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        Player player = event.getPlayer();

        if (player.isSneaking()) {
            return;
        }

        EntityEquipment equipment = player.getEquipment();

        if (!canEnchant(block)) {
            return;
        }

        ItemStack placedItem = equipment.getItem(event.getHand());

        ShulkerBoxWrapper wrapper = getShulkerBox(placedItem);

        if (wrapper == null) {
            return;
        }

        if (isEnchantmentOn(placedItem)) {
            Inventory inventory = wrapper.box().getInventory();

            List<@NotNull ItemStack> placeable = new LinkedList<>();
            for (ItemStack check : inventory) {
                if (check != null) {
                    Material placeableMaterial = getBlockMaterial(check);
                    if (placeableMaterial != null) {
                        if (block.canPlace(placeableMaterial.createBlockData())) {
                            placeable.add(check);
                        }
                    }
                }
            }

            if (!placeable.isEmpty()) {
                ItemStack chosen = WbsCollectionUtil.getRandom(placeable);

                event.setCancelled(true);

                Material material = Objects.requireNonNull(getBlockMaterial(chosen));

                // Do the update 1 tick later or the cancel will also discard this place
                WbsEnchants.getInstance().runSync(() -> {
                    BlockData newData = material.createBlockData();
                    BlockFace placedFace = event.getBlockAgainst().getFace(block);
                    if (newData instanceof Directional directional) {
                        directional.setFacing(
                                BlockUtils.getClosestFace(
                                        WbsEntityUtil.getFacingVector(player),
                                        directional.getFaces()
                                ).getOppositeFace()
                        );
                    } else if (newData instanceof Orientable orientable) {
                        if (placedFace != null) {
                            orientable.setAxis(BlockUtils.axisFromFace(placedFace));
                        }
                    }
                    block.setBlockData(newData);
                });

                if (chosen.getAmount() == 0) {
                    inventory.remove(chosen);
                } else {
                    chosen.setAmount(chosen.getAmount() - 1);
                }

                wrapper.saveToItem();
            }
        }
    }

    @Nullable
    private static Material getBlockMaterial(ItemStack check) {
        ItemType itemType = check.getType().asItemType();
        if (itemType != null && itemType.hasBlockType()) {
            BlockType blockType = itemType.getBlockType();

            // This is deprecated for internal use, but it's functionally the same as doing Material.getMaterial(blockType.key().value())
            @SuppressWarnings("deprecation")
            Material material = blockType.asMaterial();
            return material;
        }

        return null;
    }
}
