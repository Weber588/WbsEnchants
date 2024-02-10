package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.util.DamageUtils;
import wbs.utils.util.WbsItems;
import wbs.utils.util.WbsMath;

import java.util.Random;

public class LightweightEnchant extends WbsEnchantment implements DamageEnchant {
    public static final int MAX_LEVEL = 3;
    public static final double AUTOBLOCK_CHANCE = 30.0 / MAX_LEVEL;

    public LightweightEnchant() {
        super("lightweight");
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

        if (containsEnchantment(offhandItem)) {
            int level = getLevel(offhandItem);
            if (WbsMath.chance(AUTOBLOCK_CHANCE * level)) {
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

                int noDamageTicks = playerVictim.getMaximumNoDamageTicks() / ((getMaxLevel() + 1) - level);
                playerVictim.setNoDamageTicks(noDamageTicks);
                playerVictim.setCooldown(offhandItem.getType(), Math.max(noDamageTicks, 0));

                int eventDamage = (int) Math.max(Math.ceil(event.getDamage()), 1);
                WbsItems.damageItem(playerVictim, offhandItem, eventDamage, EquipmentSlot.OFF_HAND);
            }
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Lightweight";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
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
    public @NotNull String getDescription() {
        return "While holding a Lightweight shield in your offhand, you have a " + AUTOBLOCK_CHANCE + "% chance per " +
                "level of automatically blocking an attack, if you could block it with the shield.";
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Shield";
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType() == Material.SHIELD;
    }

    @Override
    public void onLootGenerate(LootGenerateEvent event) {
        if (WbsMath.chance(20)) {
            Location location = event.getLootContext().getLocation();
            World world = location.getWorld();
            if (world == null) {
                return;
            }
            if (world.getEnvironment() == World.Environment.NORMAL && location.getBlock().getY() > world.getSeaLevel()) {
                for (ItemStack stack : event.getLoot()) {
                    if (tryAdd(stack, new Random().nextInt(2) + 1)) {
                        return;
                    }
                }
            }
        }
    }
}
