package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.enchantment.helper.WbsCurse;
import wbs.utils.util.WbsMath;

import java.util.Random;

public class CurseSplintering extends WbsCurse {
    private static final String DEFAULT_DESCRIPTION = "An axe curse that causes mined logs to sometimes splinter " +
            "into sticks.";

    private static final double CHANCE_PER_LEVEL = 10;

    public CurseSplintering() {
        super("splintering", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeTagKeys.AXES);
    }

    @EventHandler
    public void onBreakLog(BlockBreakEvent event) {
        Player player = event.getPlayer();

        ItemStack enchantedItem = getIfEnchanted(player);
        if (enchantedItem != null) {
            if (Tag.LOGS.isTagged(event.getBlock().getType())) {
                if (WbsMath.chance(CHANCE_PER_LEVEL * getLevel(enchantedItem))) {
                    event.setDropItems(false);
                    Block broken = event.getBlock();
                    int numberOfSticks = new Random().nextInt(16) + 1;
                    broken.getWorld().dropItemNaturally(broken.getLocation(), ItemStack.of(Material.STICK, numberOfSticks));
                }
            }
        }
    }
}
