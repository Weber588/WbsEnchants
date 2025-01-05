package wbs.enchants.enchantment;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.util.DamageUtils;
import wbs.utils.util.WbsItems;
import wbs.utils.util.WbsMath;

public class LightweightEnchant extends WbsEnchantment implements DamageEnchant {
    public static final int MAX_LEVEL = 3;
    public static double getAutoblockChance(int level) {
        return 30.0 / level;
    }

    private static final String DEFAULT_DESCRIPTION = "While holding a Lightweight shield in your offhand, you have a "
            + getAutoblockChance(1) + "% chance per level of automatically blocking an attack, " +
            "if you could block it with the shield.";

    public LightweightEnchant() {
        super("lightweight", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(MAX_LEVEL)
                .supportedItems(WbsEnchantsBootstrap.SHIELD)
                .weight(10);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (event.getDamage() < 1) {
            return;
        }

        if (!DamageUtils.canBeBlocked(event.getCause())) {
            return;
        }

        switch (event.getCause()) {
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> {
                return;
            }
        }

        if (!(victim instanceof Player playerVictim)) {
            return;
        }

        if (playerVictim.isBlocking()) {
            return;
        }

        PlayerInventory victimInv = playerVictim.getInventory();

        ItemStack offhandItem = victimInv.getItemInOffHand();

        int offhandCooldown = playerVictim.getCooldown(offhandItem.getType());
        if (offhandCooldown > 0) {
            return;
        }

        if (isEnchantmentOn(offhandItem)) {
            int level = getLevel(offhandItem);
            if (WbsMath.chance(getAutoblockChance(maxLevel()) * level)) {
                // Check if player would be able to block

                Vector facingDirection = playerVictim.getEyeLocation().getDirection();
                Location damageLocation = attacker.getEyeLocation();
                if (projectile != null) {
                    damageLocation = projectile.getLocation();
                }

                Vector vectorToAttacker = damageLocation.subtract(playerVictim.getEyeLocation()).toVector();
                double angleToAttacker = vectorToAttacker.angle(facingDirection);

                if (angleToAttacker > Math.PI / 2) {
                    return;
                }

                // Player can block -- cancel event and fake a "quick block"

                event.setCancelled(true);

                playerVictim.swingOffHand();
                playerVictim.playEffect(EntityEffect.SHIELD_BLOCK);

                int noDamageTicks = playerVictim.getMaximumNoDamageTicks() / ((maxLevel() + 1) - level);
                playerVictim.setNoDamageTicks(noDamageTicks);
                playerVictim.setCooldown(offhandItem.getType(), Math.max(noDamageTicks, 0));

                int eventDamage = (int) Math.max(Math.ceil(event.getDamage()), 1);
                WbsItems.damageItem(playerVictim, offhandItem, eventDamage, EquipmentSlot.OFF_HAND);
            }
        }
    }
}
