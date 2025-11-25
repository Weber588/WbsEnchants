package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsItems;
import wbs.utils.util.WbsLocationUtil;
import wbs.utils.util.WbsMath;
import wbs.utils.util.persistent.BlockChunkStorageUtil;
import wbs.utils.util.persistent.WbsPersistentDataType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LavaWalkerEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "Frost walker... but for lava!";
    private static final Map<Material, Integer> REPLACEMENT_BLOCKS = Map.of(
            Material.OBSIDIAN, 9,
            Material.CRYING_OBSIDIAN, 1
    );

    public LavaWalkerEnchant() {
        super("lava_walker", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR)
                .exclusiveWith(WbsEnchantsBootstrap.COLD_BASED_ENCHANTS)
                .addInjectInto(WbsEnchantsBootstrap.HEAT_BASED_ENCHANTS);

        // TODO: exclusiveWith = Create frost walker tag, maybe either "fluid_walkers" or "heat"/"cold"?
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Map<Block, PersistentDataContainer> map = BlockChunkStorageUtil.getBlockContainerMap(event.getChunk());

        for (Block block : map.keySet()) {
            PersistentDataContainer container = map.get(block);

            NamespacedKey materialKey = container.get(getKey(), WbsPersistentDataType.NAMESPACED_KEY);

            if (materialKey != null && block.getType().getKey().equals(materialKey)) {
                block.setType(Material.LAVA);
            }

            container.remove(getKey());
            BlockChunkStorageUtil.writeContainer(block, container);
        }
    }

    @EventHandler
    public void onMovement(PlayerMoveEvent event) {
        Location to = event.getTo();

        Player player = event.getPlayer();
        // Kind of eh way to check if player is on the ground - if they're within 0.05 blocks of the top of a
        // full block, good enough.
        if (player.getLocation().getY() % 1.0 >= 0.05) {
            return;
        }

        EntityEquipment equipment = player.getEquipment();

        Location from = event.getFrom();
        if (from.getBlock().equals(to.getBlock())) {
            return;
        }

        ItemStack boots = equipment.getBoots();
        if (boots != null && isEnchantmentOn(boots)) {
            int level = getLevel(boots);

            int radius = Math.max(1, level) + 2;
            int radiusSquared = radius * radius;

            Location central = to.clone().add(BlockFace.DOWN.getDirection());
            int centralX = central.getBlockX();
            int centralY = central.getBlockY();
            int centralZ = central.getBlockZ();

            World world = player.getWorld();

            List<Block> changed = new LinkedList<>();

            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    if ((x * x) + (z * z) <= radiusSquared) {
                        Block check = world.getBlockAt(centralX + x, centralY, centralZ + z);
                        if (check.getType() == Material.LAVA) {
                            if (check.getBlockData() instanceof Levelled levelled) {
                                if (levelled.getLevel() != 0) { // Level 0 = source block
                                    continue;
                                }
                            }

                            check.setType(WbsCollectionUtil.getRandomWeighted(REPLACEMENT_BLOCKS));

                            PersistentDataContainer container = BlockChunkStorageUtil.getContainer(check);
                            container.set(getKey(), WbsPersistentDataType.NAMESPACED_KEY, check.getType().getKey());
                            BlockChunkStorageUtil.writeContainer(check, container);

                            changed.add(check);
                        }
                    }
                }
            }

            if (!changed.isEmpty()) {
                WbsItems.damageItem(player, boots, 1, EquipmentSlot.FEET);

                WbsEnchants.getInstance().runTimer(task -> {
                    if (changed.isEmpty()) {
                        task.cancel();
                        return;
                    }

                    List<Block> toRemove = new LinkedList<>();
                    for (Block block : changed) {
                        long lavaWalkerBlocksNearby = WbsLocationUtil.getNearbyBlocks(block.getLocation().toCenterLocation(), 1)
                                .stream()
                                .filter(check -> BlockChunkStorageUtil.getContainer(check).has(getKey()))
                                .count();

                        if (WbsMath.chance(12 * lavaWalkerBlocksNearby)) {
                            continue;
                        }

                        toRemove.add(block);

                        PersistentDataContainer container = BlockChunkStorageUtil.getContainer(block);

                        NamespacedKey materialKey = container.get(getKey(), WbsPersistentDataType.NAMESPACED_KEY);
                        if (block.getType().getKey().equals(materialKey)) {
                            block.setType(Material.LAVA);
                        }

                        container.remove(getKey());
                        BlockChunkStorageUtil.writeContainer(block, container);
                    }

                    changed.removeAll(toRemove);
                }, 30, 30);
            }
        }
    }
}
