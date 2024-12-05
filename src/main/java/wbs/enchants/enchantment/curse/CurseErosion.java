package wbs.enchants.enchantment.curse;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CurseErosion extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A tool curse that causes blocks to sometimes destabilise " +
            "around mined blocks!";

    private static final List<BlockFace> POSSIBLE_OFFSETS = List.of(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    );

    private static final double CHANCE_PER_LEVEL = 25;

    public CurseErosion() {
        super("curse/erosion", DEFAULT_DESCRIPTION);

        supportedItems = WbsEnchantsBootstrap.ENCHANTABLE_GROUND_MINING;
        maxLevel = 2;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Erosion";
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMine(BlockBreakEvent event) {
        Player player = event.getPlayer();

        ItemStack enchantedItem = getIfEnchanted(player);
        if (enchantedItem != null) {
            Set<Location> toDestabilise = new HashSet<>();
            Block brokenBlock = event.getBlock();

            for (int i = 0; i < getLevel(enchantedItem); i++) {
                if (WbsMath.chance(CHANCE_PER_LEVEL)) {
                    BlockFace direction = WbsCollectionUtil.getRandom(POSSIBLE_OFFSETS);

                    toDestabilise.add(brokenBlock.getLocation().add(direction.getDirection()));
                }
            }

            if (!toDestabilise.isEmpty()) {
                // Do this next tick to not get caught up with multi-block breaks that act on the first tick.
                WbsEnchants.getInstance().runSync(() -> destabililise(brokenBlock.getWorld(), toDestabilise));
            }
        }
    }

    private void destabililise(World world, Set<Location> toDestabilise) {
        for (Location location : toDestabilise) {
            Block block = location.getBlock();
            if (block.getType().isAir()) {
                continue;
            }
            // If it can't fall through the block below it anyway, don't try
            if (!world.getBlockAt(location.clone().add(0, -1, 0)).isPassable()) {
                continue;
            }

            world.spawn(location, FallingBlock.class, CreatureSpawnEvent.SpawnReason.ENCHANTMENT, (fallingBlock) -> {
                fallingBlock.setBlockData(block.getBlockData());
                fallingBlock.setBlockState(block.getState());
                block.setType(Material.AIR);
            });
        }
    }

    @Override
    public EnchantmentType getType() {
        return EnchantmentTypeManager.CURSE;
    }
}
