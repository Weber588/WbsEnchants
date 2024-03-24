package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.WbsMath;

import java.util.Random;
import java.util.Set;

public class DisarmingEnchant extends WbsEnchantment implements DamageEnchant {
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public DisarmingEnchant() {
        super("disarming");
    }

    @EventHandler
    public void catchEvent(EntityDamageByEntityEvent event) {
        onDamage(event);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(attacker instanceof Player playerAttacker) || !(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        if (!EntityUtils.willCrit(playerAttacker)) {
            return;
        }

        EntityEquipment equipment = attacker.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack item = equipment.getItemInMainHand();
        if (containsEnchantment(item)) {
            if (!WbsMath.chance(getLevel(item) * 10)) {
                return;
            }

            if (livingVictim instanceof Player playerVictim) {
                // Swap current held item with another random slot in hotbar, and then change their selected slot to a different slot at random
                PlayerInventory victimInv = playerVictim.getInventory();

                int heldItemSlot = victimInv.getHeldItemSlot();
                ItemStack held = victimInv.getItemInMainHand();

                int randomSlot;
                do {
                    randomSlot = RANDOM.nextInt(9);
                }
                while (randomSlot == heldItemSlot);

                ItemStack other = victimInv.getItem(randomSlot);

                victimInv.setItemInMainHand(other);
                victimInv.setItem(randomSlot, held);

                sendActionBar("&wDisarmed!", playerVictim);
                sendActionBar("Disarmed " + livingVictim.getName() + "!", playerAttacker);
            } else {
                EntityEquipment victimEquipment = livingVictim.getEquipment();
                if (victimEquipment != null) {
                    ItemStack held = victimEquipment.getItemInMainHand();

                    if (held.getType().isAir()) {
                        return;
                    }

                    livingVictim.getWorld().dropItemNaturally(livingVictim.getEyeLocation(), held);
                    victimEquipment.setItemInMainHand(new ItemStack(Material.AIR));

                    sendActionBar("Disarmed " + livingVictim.getName() + "!", playerAttacker);
                }
            }
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Disarming";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 3;
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
    public Set<Enchantment> getDirectConflicts() {
        return Set.of(KNOCKBACK);
    }

    @Override
    public @NotNull String getDescription() {
        return "When you critically hit a mob, you have a 10% chance of disarming it (per level). " +
                "Disarming a non-player will force it to drop the item it's holding, and disarming a player " +
                "will slightly rearrange their hotbar to force them to reselect their weapon.";
    }
}
