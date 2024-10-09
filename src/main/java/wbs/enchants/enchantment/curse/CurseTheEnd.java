package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsMath;

import java.util.List;

public class CurseTheEnd extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "An armour curse that makes you take damage from water!";

    private static final int CHANCE_PER_LEVEL_PER_TICK = 1;
    private static DamageSource damageType;

    public CurseTheEnd() {
        super("curse/the_end", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.ENCHANTABLE_ARMOR;
        maxLevel = 2;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of The End";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!player.isInWater()) {
            return;
        }

        ItemStack enchantedItem = getHighestEnchantedArmour(player);
        if (enchantedItem != null) {
            int level = getLevel(enchantedItem);
            if (WbsMath.chance(level * CHANCE_PER_LEVEL_PER_TICK)) {
                if (damageType == null) {
                    damageType = DamageSource.builder(DamageType.MAGIC).build();
                }
                player.damage(1, damageType);
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
