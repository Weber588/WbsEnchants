package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.DamageUtils;

public class HellborneEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "You have immunity to fire tick damage, and while you're on fire, " +
            "you have strength equal to the level of the enchantment.";

    public HellborneEnchant() {
        super("hellborne", EnchantmentTypeManager.ETHEREAL, DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_CHEST_ARMOR)
                .exclusiveWith(WbsEnchantsBootstrap.COLD_BASED_ENCHANTS)
                .addInjectInto(WbsEnchantsBootstrap.HEAT_BASED_ENCHANTS)
                .targetDescription("Chestplate");
    }

    @EventHandler
    public void onCombust(EntityDamageEvent event) {
        if (!DamageUtils.isHeat(event.getCause())) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack chestItem = equipment.getChestplate();

        if (chestItem != null && isEnchantmentOn(chestItem)) {
            int ticks = Math.max(20, living.getFireTicks());

            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                event.setCancelled(true);
            }

            int level = getLevel(chestItem);
            PotionEffect strengthEffect = new PotionEffect(PotionEffectType.STRENGTH, ticks, level - 1);
            living.addPotionEffect(strengthEffect);
        }
    }
}
