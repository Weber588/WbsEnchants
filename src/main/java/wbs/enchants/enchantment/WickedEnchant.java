package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.Map;

public class WickedEnchant extends WbsEnchantment implements DamageEnchant {
    private static final WbsParticleGroup HIT_EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect()
                    .setXYZ(1)
                    .setSpeed(0.2)
                    .setAmount(25),
                    Particle.TOWN_AURA
            );

    public WickedEnchant() {
        super("wicked");
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        ItemStack item = EntityUtils.getEnchantedFromSlot(attacker, this, EquipmentSlot.HAND);

        if (item != null) {
            double multiplier = 1;
            Map<UberEnchantment, Integer> enchantments = getEnchantments(item);
            for (UberEnchantment enchant : enchantments.keySet()) {
                if (enchant.isCursed()) {
                    double levelMultiplier = 0.5 + (0.5 * enchantments.get(enchant));
                    double rarityMultiplier = switch (enchant.getRarity()) {
                        case COMMON -> 0.05;
                        case UNCOMMON -> 0.1;
                        case RARE -> 0.2;
                        case VERY_RARE -> 0.5;
                        default -> 0;
                    };

                    multiplier += levelMultiplier * rarityMultiplier;
                }
            }

            if (multiplier > 1) {
                event.setDamage(event.getDamage() * multiplier);
                HIT_EFFECT.play(WbsEntityUtil.getMiddleLocation(victim));
            }
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "This enchantment greatly increases attack damage for every curse on the same item!";
    }

    @Override
    public String getDisplayName() {
        return "&7Wicked";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.VERY_RARE;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }
}
