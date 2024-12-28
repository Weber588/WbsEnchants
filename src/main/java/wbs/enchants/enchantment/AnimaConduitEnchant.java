package wbs.enchants.enchantment;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;

import java.util.Set;

public class AnimaConduitEnchant extends WbsEnchantment {
    private static final double CHANCE_PER_LEVEL = 1.5;

    private static final String DESCRIPTION = "Adds a small chance to regain health upon picking up XP.";
    private static final int EXPERIENCE_TRIGGER_THRESHOLD = 5;

    private static final WbsParticleEffect EFFECT = new NormalParticleEffect()
            .setX(1)
            .setY(2)
            .setZ(1)
            .setAmount(3);

    public AnimaConduitEnchant() {
        super("anima_conduit", DESCRIPTION);

        maxLevel = 4;
        supportedItems = ItemTypeTagKeys.ENCHANTABLE_CHEST_ARMOR;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Anima Conduit";
    }

    @EventHandler
    public void onXPPickup(PlayerPickupExperienceEvent event) {
        int gained = event.getExperienceOrb().getExperience();
        if (gained < EXPERIENCE_TRIGGER_THRESHOLD) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack activeItem = getHighestEnchanted(player, Set.of(
                EquipmentSlot.HAND,
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        ));

        if (activeItem != null) {
            if (WbsMath.chance(CHANCE_PER_LEVEL * getLevel(activeItem) * event.getExperienceOrb().getExperience())) {
                event.setCancelled(true);
                event.getExperienceOrb().remove();

                int regainAmount = (gained / EXPERIENCE_TRIGGER_THRESHOLD) + 1;
                player.heal(regainAmount, EntityRegainHealthEvent.RegainReason.MAGIC);
                EFFECT.play(Particle.HEART, WbsEntityUtil.getMiddleLocation(player));
            }
        }
    }
}
