package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.AbstractMultiBreakEnchant;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.BlockQuery;
import wbs.enchants.util.MaterialUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class BlastMinerEnchant extends AbstractMultiBreakEnchant {
    private static final String DEFAULT_DESCRIPTION = "Mines a 3x1x3 square when you mine a stone-type block, " +
            "increasing layers every level, for a maximum of 3x%max_level%x3 at level %max_level%.";

    public BlastMinerEnchant() {
        super("blast_miner", EnchantmentTypeManager.PARADOXICAL, DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeTagKeys.PICKAXES)
                .weight(3);
    }

    @Override
    protected boolean canBreak(Block block) {
        // TODO: Create a list of materials configurable with direct names or namespaced tags
        Material type = block.getType();
        if (Tag.BASE_STONE_OVERWORLD.isTagged(type)) {
            return true;
        }
        if (Tag.BASE_STONE_NETHER.isTagged(type)) {
            return true;
        }

        if (MaterialUtils.isOre(block)) {
            return false;
        }
        if (Tag.OVERWORLD_CARVER_REPLACEABLES.isTagged(type) && Tag.MINEABLE_PICKAXE.isTagged(type)) {
            return true;
        }

        return switch (type) {
            case END_STONE, COBBLESTONE, MOSSY_COBBLESTONE -> true;
            default -> false;
        };
    }

    @Override
    protected void handleBreak(@NotNull BlockBreakEvent event, @NotNull Block broken, @NotNull Player player, @NotNull ItemStack item, int level) {
        Predicate<Block> matching = this::canBreak;
        BlockFace face = getTargetBlockFace(player);

        if (face == null) {
            return;
        }

        final List<Block> blocksToBreak = new LinkedList<>();

        BlockQuery query = new BlockQuery()
                .setPredicate(matching)
                .setMaxDistance(1);

        Block current = broken;
        for (int i = 0; i < level && matching.test(current); i++) {
            List<Block> toAdd = query.getSquare(current, face);
            blocksToBreak.addAll(toAdd);
            blocksToBreak.add(current);
            current = current.getRelative(face.getOppositeFace());
        }

        blocksToBreak.remove(broken);

        BlockChanger.prepare(blocksToBreak)
                .setDelayTicks(1)
                .setToUpdatePerChunk(9)
                .setMatching(matching)
                .breakBlocks(player);

        WbsEnchants.getInstance().runSync(() -> {
            player.getPersistentDataContainer().set(getKey(), PersistentDataType.BOOLEAN, true);
            broken.getWorld().createExplosion(player,
                    broken.getLocation().toCenterLocation(),
                    2f,
                    false, // Don't set fire
                    false, // Don't break blocks (we did that already)
                    false // Don't protect player (source) from taking damage
            );
            player.getPersistentDataContainer().remove(getKey());
        });
    }

    @EventHandler
    private void onExplodeItem(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Item) {
            Entity causingEntity = event.getDamageSource().getCausingEntity();
            Entity directEntity = event.getDamageSource().getDirectEntity();

            if (causingEntity != null && causingEntity.getPersistentDataContainer().has(getKey())) {
                event.setCancelled(true);
            } else if (directEntity != null && directEntity.getPersistentDataContainer().has(getKey())) {
                event.setCancelled(true);
            }
        }
    }
}
