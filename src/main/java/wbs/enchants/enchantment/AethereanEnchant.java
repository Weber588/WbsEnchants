package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.BlockUtils;
import wbs.utils.util.entities.WbsEntityUtil;

import java.util.Objects;

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
        if (item == null || !item.getType().isBlock()) {
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
        Block targetBlock = player.getEyeLocation().add(facingVector).getBlock();

        BlockData blockData = item.getType().createBlockData();
        if (targetBlock.canPlace(blockData)) {
            if (blockData instanceof Directional directional) {
                directional.setFacing(
                        BlockUtils.getClosestFace(
                                WbsEntityUtil.getFacingVector(player),
                                directional.getFaces()
                        ).getOppositeFace()
                );
            } else if (blockData instanceof Orientable orientable) {
                orientable.setAxis(BlockUtils.getClosestAxis(facingVector));
            }
            targetBlock.setBlockData(blockData);
            player.swingMainHand();
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.getInventory().removeItem(item.asOne());
            }
        }
    }
}
