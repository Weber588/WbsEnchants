package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

public class EarthboundEnchant extends WbsEnchantment {
    public static final String DESCRIPTION = "You are immune to Levitation, but can't have " +
            "Feather Falling.";

    public EarthboundEnchant() {
        super("earthbound", DESCRIPTION);

        maxLevel = 1;
        supportedItems = ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR;
        exclusiveWith = WbsEnchantsBootstrap.EXCLUSIVE_SET_FALL_DAMAGE_AFFECTING;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Earthbound";
    }

    @EventHandler
    public void onLevitationApplied(EntityPotionEffectEvent event) {
        PotionEffect effect = event.getNewEffect();
        if (effect == null || effect.getType() != PotionEffectType.LEVITATION) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        ItemStack boots = getIfEnchanted(entity, EquipmentSlot.FEET);
        if (boots != null) {
            event.setCancelled(true);
        }
    }
}
