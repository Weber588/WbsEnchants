package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.utils.util.WbsItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DualWieldEnchant extends WbsEnchantment implements DamageEnchant {
    // TODO: Get this from player entity attack range attribute in 1.20.5
    private static final double ATTACK_RANGE = 3.5;

    private static final Map<UUID, Integer> DUAL_SWING_TASKS = new HashMap<>();

    public DualWieldEnchant() {
        super("dual_wield");
    }

    @Override
    public @NotNull String getDescription() {
        return "When an item with this enchantment is in your offhand, it swings automatically after attacking!";
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (event.getCause() == EntityDamageEvent.DamageCause.THORNS) {
            return;
        }

        EntityEquipment equipment = attacker.getEquipment();

        if (equipment == null) {
            return;
        }

        if (DUAL_SWING_TASKS.containsKey(attacker.getUniqueId())) {
            return;
        }

        ItemStack offHand = equipment.getItemInOffHand();

        if (containsEnchantment(offHand)) {
            AttributeInstance speedAttr = attacker.getAttribute(Attribute.GENERIC_ATTACK_SPEED);

            if (speedAttr == null) {
                return;
            }

            double attackDelayInTicks = Math.max(1, 20.0 / speedAttr.getValue());

            attackDelayInTicks = WbsItems.calculateAttributeModification(
                    offHand,
                    Attribute.GENERIC_ATTACK_SPEED,
                    attackDelayInTicks
            );

            int taskId = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!attacker.isValid() || attacker.isDead()) {
                        DUAL_SWING_TASKS.remove(attacker.getUniqueId());
                        return;
                    }

                    EntityEquipment updatedEquipment = attacker.getEquipment();
                    if (updatedEquipment == null) {
                        DUAL_SWING_TASKS.remove(attacker.getUniqueId());
                        return;
                    }

                    ItemStack updatedOffHand = updatedEquipment.getItemInOffHand();
                    if (!containsEnchantment(updatedOffHand)) {
                        DUAL_SWING_TASKS.remove(attacker.getUniqueId());
                        return;
                    }

                    Location eyeLoc = attacker.getEyeLocation();
                    RayTraceResult trace = attacker.getWorld().rayTrace(eyeLoc,
                            eyeLoc.getDirection(),
                            ATTACK_RANGE,
                            FluidCollisionMode.NEVER,
                            true,
                            0,
                            check -> !check.getUniqueId().equals(attacker.getUniqueId()));

                    if (trace != null && trace.getHitEntity() instanceof Damageable damageable) {
                        ItemStack mainHand = updatedEquipment.getItemInMainHand();

                        // Theoretically safe, but don't want to accidentally let it dupe lol
                        try {
                            updatedEquipment.setItemInMainHand(updatedOffHand);

                            attacker.attack(damageable);
                        //    damageable.damage(damage, attacker);
                        } finally {
                            updatedEquipment.setItemInMainHand(mainHand);
                        }
                    }

                    attacker.swingOffHand();
                    DUAL_SWING_TASKS.remove(attacker.getUniqueId());
                }
            }.runTaskLater(WbsEnchants.getInstance(), (long) attackDelayInTicks).getTaskId();

            DUAL_SWING_TASKS.put(attacker.getUniqueId(), taskId);
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Dual Wield";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
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
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return Tag.ITEMS_SWORDS.isTagged(itemStack.getType());
    }
}
