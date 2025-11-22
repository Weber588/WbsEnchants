package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public class SupersonicEnchant extends WbsEnchantment implements DamageEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "When flying faster than 20 blocks " +
            "per second, you deal 100% more damage per level. Does not work while firework boosting.";

    // TODO: Make this configurable
    private final double BLOCKS_PER_TICK_SPEED = 20;

    public SupersonicEnchant() {
        super("supersonic", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.ELYTRA)
                .maxLevel(2)
                .weight(1)
                .anvilCost(4);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!attacker.isGliding()) {
            return;
        }

        if (attacker.getVelocity().length() < BLOCKS_PER_TICK_SPEED / Ticks.TICKS_PER_SECOND) {
            return;
        }

        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        ItemStack highestEnchanted = getHighestEnchanted(attacker, item -> item.hasData(DataComponentTypes.GLIDER));
        if (highestEnchanted != null) {
            int level = getLevel(highestEnchanted);

            Collection<Firework> fireworks = attacker.getWorld().getNearbyEntitiesByType(Firework.class, attacker.getLocation(), 3);

            boolean hasFirework = fireworks.stream().anyMatch(firework -> attacker.equals(firework.getAttachedTo()));

            if (!hasFirework) {
                double damage = event.getDamage();
                event.setDamage(damage * (level + 1));
            }
        }
    }
}
