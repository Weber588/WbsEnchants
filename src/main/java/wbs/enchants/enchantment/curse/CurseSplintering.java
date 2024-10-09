package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsMath;

import java.util.List;
import java.util.Random;

public class CurseSplintering extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "An axe curse that causes mined logs to sometimes splinter " +
            "into sticks!";

    private static final double CHANCE_PER_LEVEL = 10;

    public CurseSplintering() {
        super("curse/splintering", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.AXES;
        maxLevel = 3;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Splintering";
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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull List<TagKey<Enchantment>> addToTags() {
        return List.of(
                EnchantmentTagKeys.CURSE
        );
    }
}
