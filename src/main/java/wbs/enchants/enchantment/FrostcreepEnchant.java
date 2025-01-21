package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.ShieldBlockEnchant;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.SphereParticleEffect;

public class FrostcreepEnchant extends WbsEnchantment implements ShieldBlockEnchant {
    private static final int TICKS_PER_LEVEL = 8;

    private static final String DESCRIPTION = "When you block an attack with your shield, your attackers weapon " +
            "will become cold, unable to attack for " + (TICKS_PER_LEVEL / 20.0) + " seconds per level.";

    public FrostcreepEnchant() {
        super("frostcreep", DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.SHIELD)
                .maxLevel(3);
    }

    @Override
    public void handleBlockDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Player victim, @Nullable Projectile projectile) {
        ItemStack item = getIfEnchanted(victim, EquipmentSlot.OFF_HAND);
        if (item == null) {
            item = getIfEnchanted(victim, EquipmentSlot.HAND);
            if (item == null) {
                return;
            }
        }

        int level = getLevel(item);
        if (attacker instanceof Player attackingPlayer) {
            ItemStack held = attackingPlayer.getInventory().getItemInMainHand();
            attackingPlayer.setCooldown(held.getType(), level * TICKS_PER_LEVEL);
        } else {
            attacker.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 10, level * TICKS_PER_LEVEL));
        }

        new SphereParticleEffect()
                .setRadius(victim.getHeight() / 2)
                .setDirection(attacker.getLocation().subtract(victim.getLocation()).toVector())
                .setSpeed(0.8)
                .buildAndPlay(Particle.SNOWFLAKE, WbsEntityUtil.getMiddleLocation(victim));
    }
}
