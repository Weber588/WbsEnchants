package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.type.FlowerBed;
import org.bukkit.block.data.type.Slab;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.BlockUtils;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.pluginhooks.hooks.PacketEventsWrapper;

import java.util.Objects;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class AethereanEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "You can place blocks in mid-air.";

    public AethereanEnchant() {
        super("aetherean", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_CHEST_ARMOR);
    }

    @EventHandler
    public void onRightClickBlock(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        if (event.getClickedBlock() != null) {
            return;
        }

        ItemStack item = event.getItem();
        Material placementMaterial = BlockUtils.getPlacementMaterial(item);
        if (placementMaterial == null) {
            return;
        }

        // Don't place enchanted blocks -- undefined behaviour
        if (!item.getEnchantments().isEmpty()) {
            return;
        }

        if (item.hasData(DataComponentTypes.CONTAINER)) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.ADVENTURE) {
            return;
        }

        if (getSumLevels(player) == 0) {
            return;
        }

        double blockReach = Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).getValue();

        // Get block in front of max reach -- player would usually need a block to place against
        Vector facingVector = WbsEntityUtil.getFacingVector(player, blockReach - 1);
        Location targetLocation = player.getEyeLocation().add(facingVector);
        Block targetBlock = targetLocation.getBlock();

        try {
            tryNMSPlace(player, facingVector, item, targetLocation, targetBlock);
            return;
        } catch (Exception ignored) {}

        PacketEventsWrapper pe = PacketEventsWrapper.get().orElse(null);
        if (pe != null) {
            RayTraceResult rayTraceResult = player.rayTraceBlocks(blockReach);
            pe.simulatePlayerInteract(EquipmentSlot.HAND, rayTraceResult, blockReach, player);
        } else {
            tryBukkitPlace(placementMaterial, targetBlock, facingVector, player, item);
        }
    }

    private static void tryBukkitPlace(Material placementMaterial, Block targetBlock, Vector facingVector, Player player, ItemStack item) {
        int y = targetBlock.getY();
        World world = targetBlock.getWorld();
        if (y > world.getMaxHeight() || y < world.getMinHeight()) {
            return;
        }

        BlockData blockData = placementMaterial.createBlockData();
        if (targetBlock.canPlace(blockData)) {
            boolean placingInSameBlock = targetBlock.getType() == blockData.getMaterial();

            BlockData targetBlockData = targetBlock.getBlockData();
            if (!targetBlockData.isReplaceable()) {
                if (!placingInSameBlock) {
                    return;
                }

                boolean allowPlaceInside = false;

                if (blockData instanceof Slab && targetBlockData instanceof Slab targetSlab) {
                    if (targetSlab.getType() == Slab.Type.DOUBLE) {
                        return;
                    }

                    targetSlab.setType(Slab.Type.DOUBLE);
                    blockData = targetSlab;
                    allowPlaceInside = true;
                }

                if (blockData instanceof FlowerBed flowerBed && targetBlockData instanceof FlowerBed targetBed) {
                    if (targetBed.getFlowerAmount() >= targetBed.getMaximumFlowerAmount()) {
                        return;
                    }

                    flowerBed.setFlowerAmount(targetBed.getFlowerAmount() + flowerBed.getFlowerAmount());
                    allowPlaceInside = true;
                }

                if (!allowPlaceInside) {
                    return;
                }
            }

            Axis closestAxis = BlockUtils.getClosestAxis(facingVector);

            if (blockData instanceof Directional directional) {
                directional.setFacing(
                        BlockUtils.getClosestFace(
                                WbsEntityUtil.getFacingVector(player),
                                directional.getFaces()
                        ).getOppositeFace()
                );
            } else if (blockData instanceof Orientable orientable) {
                orientable.setAxis(closestAxis);
            }

            targetBlock.setBlockData(blockData);
            player.swingMainHand();
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.getInventory().removeItem(item.asOne());
            }
        }
    }

    private void tryNMSPlace(Player player, Vector facingVector, ItemStack item, Location targetLocation, Block targetBlock) {
        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.fromBukkitCopy(item);
        Item itemDef = nmsItem.getItem();
        if (!(itemDef instanceof BlockItem blockItem)) {
            return;
        }

        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        Direction direction = CraftBlock.blockFaceToNotch(BlockUtils.getClosestFace(facingVector, Set.of(
                BlockFace.UP,
                BlockFace.DOWN,
                BlockFace.NORTH,
                BlockFace.EAST,
                BlockFace.SOUTH,
                BlockFace.WEST
        )));
        if (direction == null) {
            direction = Direction.UP;
        }

        UseOnContext useOnContext = new UseOnContext(serverPlayer,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        new Vec3(targetLocation.x(), targetLocation.y(), targetLocation.z()),
                        direction,
                        new BlockPos(targetBlock.getX(), targetLocation.getBlockY(), targetLocation.getBlockZ()),
                        true
                )
        );

        InteractionResult result = blockItem.place(new BlockPlaceContext(useOnContext));
        if (result == InteractionResult.SUCCESS) {
            player.swingMainHand();
        }
    }
}
