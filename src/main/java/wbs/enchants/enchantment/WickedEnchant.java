package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
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
import wbs.enchants.util.EnchantUtils;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.Map;

public class WickedEnchant extends WbsEnchantment implements DamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "This enchantment greatly increases attack damage for every " +
            "curse on the same item!";

    private static final WbsParticleGroup HIT_EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect()
                    .setXYZ(1)
                    .setSpeed(0.2)
                    .setAmount(25),
                    Particle.MYCELIUM
            );

    public WickedEnchant() {
        super("wicked", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.ENCHANTABLE_WEAPON;
        weight = 1;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Wicked";
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        ItemStack item = EntityUtils.getEnchantedFromSlot(attacker, this, EquipmentSlot.HAND);

        if (item != null) {
            double multiplier = 1;
            Map<Enchantment, Integer> enchantments = item.getEnchantments();

            for (Enchantment enchant : enchantments.keySet()) {
                if (EnchantUtils.isCurse(enchant)) {
                    double levelMultiplier = 0.5 + (0.5 * enchantments.get(enchant));

                    double rarityMultiplier = 1.0 / enchant.getWeight();

                    multiplier += levelMultiplier * rarityMultiplier;
                }
            }

            if (multiplier > 1) {
                event.setDamage(event.getDamage() * multiplier);
                HIT_EFFECT.play(WbsEntityUtil.getMiddleLocation(victim));
            }
        }
    }
}
