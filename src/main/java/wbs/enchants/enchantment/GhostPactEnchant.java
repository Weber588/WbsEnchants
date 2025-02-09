package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;

import java.util.Random;

public class GhostPactEnchant extends WbsEnchantment implements DamageEnchant {
    private static final double CHANCE_PER_HIT = 5;

    private static final String DEFAULT_DESCRIPTION = "When you take more than 2 hearts of damage in a single hit, you " +
            "have a chance of summoning your own Vex to engage your attacker!";

    public GhostPactEnchant() {
        super("ghost_pact", EnchantmentTypeManager.ETHEREAL, DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(4)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_ARMOR_RETALIATION)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_ARMOR);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        ItemStack item = getHighestEnchantedArmour(livingVictim);

        if (item != null) {
            int level = getLevel(item);

            if (WbsMath.chance(CHANCE_PER_HIT)) {
                int spawnCount = Math.min(1, (new Random().nextInt(level) + 1) / 2);

                for (int i = 0; i < spawnCount; i++) {
                    Vex vex = victim.getWorld().spawn(
                            WbsEntityUtil.getMiddleLocation(victim),
                            Vex.class,
                            CreatureSpawnEvent.SpawnReason.ENCHANTMENT
                    );


                    // Summoner is stored as Mob, which LivingEntity/Player don't extend, so worst case
                    // the vex might turn around and hit the wearer themselves idk
                    vex.setLimitedLifetime(true);
                    vex.setLimitedLifetimeTicks(100);
                    vex.setTarget(attacker);
                    
                    vex.setCharging(true);
                }
            }
        }
    }
}
