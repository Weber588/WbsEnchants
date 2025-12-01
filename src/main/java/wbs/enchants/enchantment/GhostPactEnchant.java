package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.persistent.WbsPersistentDataType;

import java.util.Random;
import java.util.UUID;

public class GhostPactEnchant extends WbsEnchantment implements DamageEnchant {
    private static final NamespacedKey SUMMONER_KEY = WbsEnchantsBootstrap.createKey("ghost_pact_summoner");
    private static final NamespacedKey TARGET_KEY = WbsEnchantsBootstrap.createKey("ghost_pact_target");

    private static final double CHANCE_PER_HIT = 5;

    private static final String DEFAULT_DESCRIPTION = "When taking damage, you " +
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

            if (WbsMath.chance(CHANCE_PER_HIT * level)) {
                int spawnCount = Math.max(1, (new Random().nextInt(level) + 1) * 2 / 3);

                for (int i = 0; i < spawnCount; i++) {
                    Vex vex = victim.getWorld().spawn(
                            WbsEntityUtil.getMiddleLocation(victim),
                            Vex.class,
                            CreatureSpawnEvent.SpawnReason.ENCHANTMENT,
                            summoned -> {
                                summoned.setLimitedLifetime(true);
                                summoned.setLimitedLifetimeTicks(200);
                                summoned.setTarget(attacker);
                                if (livingVictim instanceof Mob mob) {
                                    summoned.setSummoner(mob);
                                }

                                AttributeInstance movementAttribute = summoned.getAttribute(Attribute.MOVEMENT_SPEED);
                                if (movementAttribute == null) {
                                    movementAttribute = summoned.getAttribute(Attribute.FLYING_SPEED);
                                    if (movementAttribute == null) {
                                        WbsEnchants.getInstance().getLogger().info("Vex had no movement attribute!");
                                    }
                                }

                                if (movementAttribute != null) {
                                    AttributeModifier modifier = new AttributeModifier(
                                            WbsEnchantsBootstrap.createKey(""),
                                            2,
                                            AttributeModifier.Operation.MULTIPLY_SCALAR_1
                                    );
                                    movementAttribute.addModifier(modifier);
                                }
                            }
                    );


                    // Summoner is stored as Mob, which LivingEntity/Player don't extend, so worst case
                    // the vex might turn around and hit the wearer themselves idk

                    PersistentDataContainer vexContainer = vex.getPersistentDataContainer();

                    vexContainer.set(SUMMONER_KEY, WbsPersistentDataType.UUID, livingVictim.getUniqueId());
                    vexContainer.set(TARGET_KEY, WbsPersistentDataType.UUID, attacker.getUniqueId());

                    vex.setCharging(true);
                }
            }
        }
    }

    @EventHandler
    public void targetEvent(EntityTargetLivingEntityEvent event) {
        Entity entity = event.getEntity();
        PersistentDataContainer container = entity.getPersistentDataContainer();

        UUID summonerUUID = container.get(SUMMONER_KEY, WbsPersistentDataType.UUID);
        UUID targetUUID = container.get(TARGET_KEY, WbsPersistentDataType.UUID);

        Entity preferredTarget = null;
        if (targetUUID != null) {
            preferredTarget = Bukkit.getEntity(targetUUID);
            if (preferredTarget == null || !preferredTarget.isValid() || preferredTarget.isDead()) {
                preferredTarget = null;
            }
        }

        if (preferredTarget != null) {
            event.setTarget(preferredTarget);
        } else {
            LivingEntity target = event.getTarget();
            if (target != null) {
                if (target.getUniqueId().equals(summonerUUID)) {
                    event.setTarget(null);

                    Entity summoner = Bukkit.getEntity(summonerUUID);
                    if (summoner != null && summoner.isValid() && !summoner.isDead()) {
                        EntityDamageEvent lastDamageCause = summoner.getLastDamageCause();
                        if (lastDamageCause != null) {
                            Entity lastAttacker = lastDamageCause.getDamageSource().getCausingEntity();
                            event.setTarget(lastAttacker);
                        }
                    }
                }
            }
        }
    }
}
