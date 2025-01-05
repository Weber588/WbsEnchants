package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsItems;

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
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR);

        // TODO: exclusiveWith = Create frost walker tag, maybe either "fluid_walkers" or "heat"/"cold"?
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

            int changed = 0;

            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    if ((x * x) + (z * z) <= radiusSquared) {
                        Block check = world.getBlockAt(centralX + x, centralY, centralZ + z);
                        if (check.getType() == Material.LAVA) {
                            check.setType(WbsCollectionUtil.getRandomWeighted(REPLACEMENT_BLOCKS));
                            changed++;
                        }
                    }
                }
            }

            if (changed > 0) {
                WbsItems.damageItem(player, boots, 1, EquipmentSlot.FEET);
            }
        }
    }
}
