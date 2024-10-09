package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

import java.util.List;

public class CurseVoiding extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A bucket curse that causes any liquids picked up to simply " +
            "disappear into the void!";

    public CurseVoiding() {
        super("curse/voiding", DEFAULT_DESCRIPTION);

        supportedItems = WbsEnchantsBootstrap.BUCKET;
        maxLevel = 1;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Voiding";
    }

    @EventHandler
    public void onFillBucket(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();

        ItemStack enchantedItem = getIfEnchanted(player, event.getHand());
        if (enchantedItem != null) {
            // Return original item, so it does... nothing
            event.setItemStack(enchantedItem);
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
