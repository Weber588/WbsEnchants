package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;

public class ArcticStrikeEnchant extends WbsEnchantment implements DamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "Gradually freezes hit mobs.";
    private static final WbsParticleEffect EFFECT = new NormalParticleEffect().setAmount(5);

    public ArcticStrikeEnchant() {
        super("arctic_strike", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON)
                .exclusiveWith(WbsEnchantsBootstrap.HEAT_BASED_ENCHANTS)
                .addInjectInto(WbsEnchantsBootstrap.COLD_BASED_ENCHANTS)
                .minimumCost(5, 6)
                .maximumCost(25, 6);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        ItemStack item = getIfEnchanted(attacker);

        if (item == null) {
            return;
        }

        int level = getLevel(item);
        int currentFreezeTicks = victim.getFreezeTicks();
        int toAdd = victim.getMaxFreezeTicks() / 10 * level;

        victim.setFreezeTicks(Math.min(currentFreezeTicks * 2, currentFreezeTicks + toAdd));

        EFFECT.play(Particle.SNOWFLAKE, WbsEntityUtil.getMiddleLocation(victim));
    }
}
