package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

// TODO: Change this to be a single level enchant that always resets a creepers fuse ticks, but doesn't increase max ticks.
//  Maybe 2 levels, where level 1 requires crits, level 2 doesn't?
public class DefusalEnchant extends WbsEnchantment implements DamageEnchant {
    private static final int DEFAULT_CREEPER_FUSE_TICKS = 30;
    private static final int CHANCE_PER_LEVEL = 25;
    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setXYZ(0.5).setY(1).setSpeed(0.1).setAmount(3), Particle.CLOUD);

    private static final String DEFAULT_DESCRIPTION = "When hitting a creeper that's preparing to explode, you have a " + CHANCE_PER_LEVEL + "% chance " +
            "per level to defuse it, instantly restarting its fuse time to zero. After a successful defusal, " +
            "the creeper's time to explode increases by 100% for each level above level 1.";

    public DefusalEnchant() {
        super("defusal", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .weight(5)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_DEFUSAL);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(victim instanceof Creeper creeper)) {
            return;
        }

        if (creeper.getFuseTicks() <= 0) {
            return;
        }

        EntityEquipment equipment = attacker.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack item = equipment.getItemInMainHand();
        if (isEnchantmentOn(item)) {
            int level = getLevel(item);
            if (WbsMath.chance(CHANCE_PER_LEVEL * level)) {
                creeper.setFuseTicks(0);
                creeper.setMaxFuseTicks(Math.max(DEFAULT_CREEPER_FUSE_TICKS, DEFAULT_CREEPER_FUSE_TICKS * level));
                EFFECT.play(creeper.getEyeLocation());

                new RadiusSelector<>(Player.class)
                        .setRange(15)
                        .select(creeper)
                        .forEach(player -> player.stopSound(Sound.ENTITY_CREEPER_PRIMED));
            }
        }
    }
}
