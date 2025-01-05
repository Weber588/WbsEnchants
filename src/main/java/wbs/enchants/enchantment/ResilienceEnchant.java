package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.DamageEnchant;

public class ResilienceEnchant extends WbsEnchantment implements DamageEnchant {
    private static final int DEFAULT_INVULN_TICKS = 10;
    private static final int BONUS_TICKS_PER_LEVEL = 3;

    private static final String DEFAULT_DESCRIPTION = "When you take damage, instead of the regular " +
            DEFAULT_INVULN_TICKS / 20.0 + " seconds of invulnerability after taking damage, you're invulnerable " +
            "for an additional " + BONUS_TICKS_PER_LEVEL / 20.0 + " seconds per level";

    public ResilienceEnchant() {
        super("resilience", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_ARMOR)
                .exclusiveInject(EnchantmentTagKeys.EXCLUSIVE_SET_ARMOR)
                .weight(5);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(victim instanceof LivingEntity entity)) {
            return;
        }

        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) {
            return;
        }

        int totalLevels = 0;
        for (ItemStack item : equipment.getArmorContents()) {
            if (item != null && isEnchantmentOn(item)) {
                totalLevels += Math.max(1, getLevel(item));
            }
        }

        if (totalLevels > 0) {
            int bonusTicks = totalLevels * BONUS_TICKS_PER_LEVEL;

            // Run next tick to avoid interfering with this damage action
            WbsEnchants.getInstance().runSync(() -> entity.setNoDamageTicks(bonusTicks));
        }
    }
}
