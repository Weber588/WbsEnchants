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
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.DamageUtils;

public class HellborneEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "You have strength and immunity to fire tick damage, " +
            "with strength level relating to the level of the enchantment while you're on fire.";

    public HellborneEnchant() {
        super("hellborne", DEFAULT_DESCRIPTION);

        maxLevel = 2;
        supportedItems = ItemTypeTagKeys.ENCHANTABLE_CHEST_ARMOR;
        weight = 1;

        targetDescription = "Chestplate";
    }

    @Override
    public String getDefaultDisplayName() {
        return "Hellborne";
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

    @Override
    public EnchantmentType getType() {
        return EnchantmentTypeManager.ETHEREAL;
    }
}
