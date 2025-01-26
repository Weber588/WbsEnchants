package wbs.enchants.enchantment.curse;

import io.papermc.paper.event.entity.EntityDamageItemEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.WbsCurse;
import wbs.utils.util.WbsMath;

public class CurseRust extends WbsCurse {
    private static final String DEFAULT_DESCRIPTION = "The item will break more quickly.";

    public CurseRust() {
        super("rust", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_RUSTABLE);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onDamageItem(EntityDamageItemEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }
        ItemStack item = event.getItem();

        if (isEnchantmentOn(item)) {
            int level = getLevel(item);

            if (WbsMath.chance(100 - (100.0 / (level + 1)))) {
                // Double the damage
                int damage = event.getDamage();
                item.damage(damage, livingEntity);
            }
        }
    }
}
