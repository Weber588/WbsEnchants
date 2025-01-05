package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

public class VampiricEnchant extends WbsEnchantment implements DamageEnchant {
    public static final int POTION_HEALTH_BOOST_LEVEL_HEARTS = 4;
    public static final double HEAL_PERCENT = 33;
    public static final int DURATION_PER_LEVEL = 10 * 20;
    public static final int CHANCE_PER_LEVEL = 20;

    private static final String DEFAULT_DESCRIPTION = "When you damage a mob, you have a " + CHANCE_PER_LEVEL +
            "% chance per level to gain " + HEAL_PERCENT + "% of the damage dealt, that expires after "
            + DURATION_PER_LEVEL / 20 + " seconds (per level).";

    // TODO: Check if this causes issues when the game runs slower? Should it be based on Bukkit.getCurrentTick()?
    private static final NamespacedKey EXPIRE_TIME_KEY = new NamespacedKey("wbsenchants", "vampiric_expire_time");

    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setXYZ(0.25).setY(0.5).setSpeed(0).setAmount(15), Particle.DAMAGE_INDICATOR);

    public VampiricEnchant() {
        super("vampiric", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .weight(5)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        EntityEquipment equipment = attacker.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack item = equipment.getItemInMainHand();
        if (isEnchantmentOn(item)) {
            int level = getLevel(item);
            if (WbsMath.chance(CHANCE_PER_LEVEL * level)) {
                double damageDealt = event.getDamage();

                int healthBoostLevel = (int) Math.ceil(damageDealt / POTION_HEALTH_BOOST_LEVEL_HEARTS / (100.0 / HEAL_PERCENT));

                int durationInTicks = DURATION_PER_LEVEL * level;

                PotionEffect healthBoostEffect = new PotionEffect(PotionEffectType.HEALTH_BOOST,
                        durationInTicks,
                        healthBoostLevel,
                        false, false, false);

                // Just for aesthetic -- we'll cancel the damage.
                PotionEffect witherEffect = new PotionEffect(PotionEffectType.WITHER,
                        durationInTicks,
                        0,
                        false, false, false);

                attacker.addPotionEffect(healthBoostEffect);
                attacker.addPotionEffect(witherEffect);
                attacker.heal(healthBoostLevel, EntityRegainHealthEvent.RegainReason.MAGIC);

                PersistentDataContainer dataContainer = attacker.getPersistentDataContainer();

                long expireTime = Bukkit.getCurrentTick() + ((long) durationInTicks * (1000 / 20));
                dataContainer.set(EXPIRE_TIME_KEY, PersistentDataType.LONG, expireTime);

                EFFECT.play(WbsEntityUtil.getMiddleLocation(victim));
            }
        }
    }

    @EventHandler
    public void onWitherDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.WITHER) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PersistentDataContainer dataContainer = player.getPersistentDataContainer();

        Long vampiricTimestamp = dataContainer.get(EXPIRE_TIME_KEY, PersistentDataType.LONG);

        if (vampiricTimestamp == null) {
            return;
        }

        if (vampiricTimestamp > Bukkit.getCurrentTick()) {
            event.setCancelled(true);
        } else {
            dataContainer.remove(EXPIRE_TIME_KEY);
        }
    }
}
