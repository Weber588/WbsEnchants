package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.type.EnchantmentTypeManager;

public class FangedEnchant extends WbsEnchantment implements DamageEnchant {
    private static final int MAX_COOLDOWN_TICKS = 100;

    private static final String DEFAULT_DESCRIPTION = "After dealing damage, evoker fangs appear at the hit entity. Cooldown reduces with level.";

    public FangedEnchant() {
        super("fanged", EnchantmentTypeManager.ETHEREAL, DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        ItemStack item = getIfEnchanted(attacker, EquipmentSlot.HEAD);

        if (item != null) {
            if (newCooldown(attacker, MAX_COOLDOWN_TICKS / getLevel(item))) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location hitLocation = livingVictim.getLocation();
                        hitLocation.getWorld().spawn(
                                hitLocation,
                                EvokerFangs.class,
                                CreatureSpawnEvent.SpawnReason.ENCHANTMENT,
                                fangs -> fangs.setOwner(attacker)
                        );
                    }
                }.runTaskLater(WbsEnchants.getInstance(), livingVictim.getMaximumNoDamageTicks() + 1);
            }
        }
    }


}
