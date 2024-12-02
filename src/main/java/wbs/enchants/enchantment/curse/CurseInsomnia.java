package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

import java.util.List;

public class CurseInsomnia extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "An armour curse that prevents the player from " +
            "sleeping while worn.";

    public CurseInsomnia() {
        super("curse/insomnia", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Insomnia";
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        // Just check if there's ANY armour with this enchant on it, don't care about details
        if (getHighestEnchantedArmour(player) != null) {
            event.setCancelled(true);
            sendActionBar("&c" + getDisplayName() + "&7 prevents your sleep...", player);
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
