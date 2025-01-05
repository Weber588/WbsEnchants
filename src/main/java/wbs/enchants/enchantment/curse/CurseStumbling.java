package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.EntityUtils;

public class CurseStumbling extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A curse that causes increased fall damage when worn on boots; " +
            "essentially the opposite of Feather Falling.";

    public CurseStumbling() {
        super("curse/stumbling", EnchantmentTypeManager.CURSE, "Curse of Stumbling", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(4)
                .weight(10)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_FALL_DAMAGE_AFFECTING);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        ItemStack boots = EntityUtils.getEnchantedFromSlot(entity, this, EquipmentSlot.FEET);

        if (boots != null) {
            double damage = event.getDamage();
            damage *= (1 + (0.12 * getLevel(boots)));
            event.setDamage(damage);
        }
    }
}
