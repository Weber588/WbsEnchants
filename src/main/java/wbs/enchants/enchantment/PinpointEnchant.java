package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.entities.WbsEntityUtil;

public class PinpointEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "Removes all inaccuracy from fired arrows.";

    public PinpointEnchant() {
        super("pinpoint", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_BOW)
                .weight(3);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        Entity shot = event.getProjectile();
        if (!(shot instanceof Projectile projectile)) {
            // Not sure why getProjectile() isn't... y'know. A projectile.
            return;
        }

        ItemStack item = event.getBow();
        if (item == null) {
            item = event.getConsumable();
            if (item == null) {
                return;
            }
        }

        if (isEnchantmentOn(item)) {
            LivingEntity shooter = event.getEntity();
            Vector newVelocity = WbsEntityUtil.getFacingVector(shooter, projectile.getVelocity().length());

            projectile.setVelocity(newVelocity);
        }
    }
}
