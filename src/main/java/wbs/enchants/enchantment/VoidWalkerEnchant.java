package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsItems;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

public class VoidWalkerEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "When you fall into the void, you're immediately teleported to " +
            "the top of the world - get that bucket clutch ready!";

    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setXYZ(0.5).setY(1.5), Particle.WITCH)
            .addEffect(new NormalParticleEffect().setXYZ(0.05).setY(0.5), Particle.LARGE_SMOKE);


    public VoidWalkerEnchant() {
        super("void_walker", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR);
    }

    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack boots = equipment.getBoots();
        if (boots != null && isEnchantmentOn(boots)) {
            int maxHeight = living.getWorld().getMaxHeight();
            Location teleportLoc = living.getLocation();
            teleportLoc.setY(maxHeight + living.getHeight());

            EFFECT.play(living.getLocation());
            living.teleport(teleportLoc);
            EFFECT.play(living.getLocation());

            if (living instanceof Player player) {
                int eventDamage = (int) Math.max(Math.ceil(event.getDamage()), 1);

                WbsItems.damageItem(player, boots, eventDamage, EquipmentSlot.FEET);
            }
        }
    }
}
