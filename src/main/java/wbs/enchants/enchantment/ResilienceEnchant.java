package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;

import java.util.Set;

public class ResilienceEnchant extends WbsEnchantment implements DamageEnchant {
    private static final int DEFAULT_INVULN_TICKS = 10;
    private static final int BONUS_TICKS_PER_LEVEL = 3;

    public ResilienceEnchant() {
        super("resilient");
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
            if (item != null && containsEnchantment(item)) {
                totalLevels += Math.max(1, getLevel(item));
            }
        }

        if (totalLevels > 0) {
            int bonusTicks = totalLevels * BONUS_TICKS_PER_LEVEL;

            // Run next tick to avoid interfering with this damage action
            plugin.runSync(() -> entity.setNoDamageTicks(bonusTicks));
        }
    }

    @Override
    public @NotNull String getDescription() {
        double defaultSeconds = DEFAULT_INVULN_TICKS / 20.0;
        double bonusSeconds = BONUS_TICKS_PER_LEVEL / 20.0;

        double maxBonus = defaultSeconds + (bonusSeconds * 4 * getMaxLevel());

        return "When you take damage, instead of the regular " + defaultSeconds + " seconds of invulnerability " +
                "after taking damage, you're invulnerable for an additional " + bonusSeconds + " seconds " +
                "per level, for a maximum of " + maxBonus + " seconds of invulnerability with level " + getMaxLevel() +
                " on all armour pieces.";
    }

    @Override
    public String getDisplayName() {
        return "&7Resilience";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.RARE;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public Set<Enchantment> getIndirectConflicts() {
        return Set.of(PROTECTION_ENVIRONMENTAL);
    }
}
