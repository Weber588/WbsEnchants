package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;

public class PinpointEnchant extends WbsEnchantment implements DamageEnchant {
    public PinpointEnchant() {
        super("pinpoint");
        registerDamageEvent();
    }

    @Override
    public @NotNull String getDescription() {
        return "Arrows like needles mean the damage is highly localised, making it possible to hit the same entity " +
                "with multiple arrows in quick succession, or even at the same time.";
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (projectile == null) {
            return;
        }
        if (!(victim instanceof LivingEntity hit)) {
            return;
        }

        PersistentDataContainer container = projectile.getPersistentDataContainer();
        Boolean isPinpoint = container.get(getKey(), PersistentDataType.BOOLEAN);

        // Unsure why "isPinpoint == true" doesn't allow null OR false to return true? Guess I need to study Java more lol
        if (isPinpoint != null && isPinpoint) {
            hit.setNoDamageTicks(0);
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        Entity shot = event.getProjectile();
        if (!(shot instanceof Projectile projectile)) {
            // Not sure why getProjectile() isn't... y'know. A projectile.
            return;
        }

        ProjectileSource source = projectile.getShooter();

        ItemStack item = event.getBow();
        if (item == null) {
            item = event.getConsumable();
            if (item == null) {
                return;
            }
        }

        if (containsEnchantment(item)) {
            PersistentDataContainer container = projectile.getPersistentDataContainer();

            container.set(getKey(), PersistentDataType.BOOLEAN, true);
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Pinpoint";
    }

    @Override
    public Rarity getRarity() {
        return null;
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
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
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }
}
