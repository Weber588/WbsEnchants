package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.TileState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.enchants.enchantment.helper.FishingEnchant;
import wbs.utils.util.WbsLocationUtil;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.persistent.BlockChunkStorageUtil;

import java.util.Set;
import java.util.function.Predicate;

public class HaulingEnchant extends WbsEnchantment implements FishingEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Greatly increases the pull strength of fishing rods.";

    public HaulingEnchant() {
        super("hauling", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_FISHING)
                .maxLevel(3);
    }

    @Override
    public void onFishEvent(PlayerFishEvent event, ItemStack rod, EquipmentSlot hand) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY && event.getState() != PlayerFishEvent.State.REEL_IN) {
            return;
        }

        FishHook hook = event.getHook();
        Entity hookedEntity = hook.getHookedEntity();
        if (hookedEntity instanceof Item) {
            return;
        }

        Vector hookToPlayer = event.getPlayer().getLocation().subtract(hook.getLocation()).toVector();
        Vector velocity = WbsMath.scaleVector(
                hookToPlayer,
                0.1 + 0.4 * getLevel(rod)
        );

        if (hookedEntity == null) {
            if (hook.getVelocity().lengthSquared() < 0.1 && !WbsEntityUtil.isInWater(hook)) {
                Set<Block> intersectingBlocks = WbsLocationUtil.getIntersectingBlocks(
                        hook.getBoundingBox().expand(Double.MIN_VALUE),
                        WbsEntityUtil.getMiddleLocation(hook)
                );

                intersectingBlocks.stream()
                        .filter(block -> !(block.getState() instanceof TileState) && (
                                block.getPistonMoveReaction() == PistonMoveReaction.MOVE || block.getPistonMoveReaction() == PistonMoveReaction.PUSH_ONLY
                            )
                        )
                        .filter(Predicate.not(BlockEnchant::hasBlockEnchants))
                        .filter(block -> BlockChunkStorageUtil.getContainer(block).isEmpty())
                        .findFirst().ifPresent(block -> {
                            block.getWorld().spawn(block.getLocation().add(0.5, 0, 0.5), FallingBlock.class, CreatureSpawnEvent.SpawnReason.ENCHANTMENT, (fallingBlock) -> {
                                fallingBlock.setBlockData(block.getBlockData());
                                fallingBlock.setBlockState(block.getState());
                                fallingBlock.setVelocity(velocity);
                            });
                            block.setType(Material.AIR);
                        }
                );
            }
        } else {
            // Standard hook pull sets velocity, doesn't add to it -- use custom implementation
            hookedEntity.setVelocity(velocity);
        }
    }
}
