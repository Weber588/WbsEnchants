package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.persistent.BlockChunkStorageUtil;

import java.util.Set;

public class DemetrienEnchant extends WbsEnchantment {
    private static final Set<Material> TILLABLE = Set.of(
            Material.DIRT,
            Material.DIRT_PATH,
            Material.GRASS_BLOCK
    );

    public static final String DESCRIPTION = "Farmland tilled by items with this enchantment " +
            "will never turn back into dirt.";

    public DemetrienEnchant() {
        super("demetrien", DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.HOES);
    }

    @EventHandler
    public void onPlayerUpdateFarmland(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        if (TILLABLE.contains(block.getType())) {
            if (event.getEntity() instanceof LivingEntity entity) {
                ItemStack held = getIfEnchanted(entity);

                if (held != null) {
                    PersistentDataContainer container = BlockChunkStorageUtil.getContainer(block);

                    container.set(getKey(), PersistentDataType.BOOLEAN, true);

                    BlockChunkStorageUtil.writeContainer(block, container);
                }
            }
        } else if (block.getType() == Material.FARMLAND && event.getTo() != Material.FARMLAND) {
            PersistentDataContainer container = BlockChunkStorageUtil.getContainer(block);

            if (container.has(getKey())) {
                event.setCancelled(true);
            }
        }
    }

    // Awful event name :(
    @EventHandler
    public void onFarmlandDry(BlockFadeEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.FARMLAND) {
            if (event.getNewState().getType() != Material.FARMLAND) {
                PersistentDataContainer container = BlockChunkStorageUtil.getContainer(block);

                if (container.has(getKey())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        PersistentDataContainer container = BlockChunkStorageUtil.getContainer(block);

        if (container.has(getKey())) {
            container.remove(getKey());
            BlockChunkStorageUtil.writeContainer(block, container);
        }
    }
}
