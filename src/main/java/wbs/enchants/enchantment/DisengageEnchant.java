package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;

public class DisengageEnchant extends WbsEnchantment {
    private static final int TICKS_PER_LEVEL = 10;

    private static final String DESCRIPTION = "After killing a mob, you cannot take damage for "
            + (TICKS_PER_LEVEL / 20.0) + " seconds.";

    public DisengageEnchant() {
        super("disengage", DESCRIPTION);

        supportedItems = ItemTypeTagKeys.SWORDS;
        maxLevel = 3;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Disengage";
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        Player killer = entity.getKiller();

        if (killer == null) {
            return;
        }

        ItemStack item = getIfEnchanted(killer);
        if (item != null) {
            int invulnerabilityTicks = TICKS_PER_LEVEL * getLevel(item);

            killer.setNoDamageTicks(invulnerabilityTicks);

            new NormalParticleEffect().setXYZ(1)
                    .setSpeed(0.3)
                    .setAmount(25)
                    .play(Particle.ENCHANT, WbsEntityUtil.getMiddleLocation(killer));
        }
    }
}
