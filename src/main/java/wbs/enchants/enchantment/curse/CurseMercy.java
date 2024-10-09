package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

import java.util.List;

public class CurseMercy extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A weapon curse that refuses to deal a killing blow.";

    public CurseMercy() {
        super("curse/mercy", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.ENCHANTABLE_WEAPON;
        maxLevel = 1;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Mercy";
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDeathEvent event) {
        if (!(event.getDamageSource().getCausingEntity() instanceof Player killer)) {
            return;
        }

        ItemStack enchantedItem = getIfEnchanted(killer);
        if (enchantedItem != null) {
            event.setCancelled(true);
            event.setReviveHealth(2);
            sendActionBar("Your weapon refuses to deal the killing blow...", killer);
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
