package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
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

public class DisarmingEnchant extends WbsEnchantment implements DamageEnchant {
    private static final int CHANCE_PER_LEVEL = 10;
    private static final String DEFAULT_DESCRIPTION = "When you critically hit a mob, you have a " + CHANCE_PER_LEVEL +
            "% chance of disarming it (per level). Disarming a non-player will force it to drop the item it's holding, " +
            "and disarming a player will slightly rearrange their hotbar to force them to reselect their weapon.";

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private boolean disarmNamedMobs = false;

    public DisarmingEnchant() {
        super("disarming", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .weight(10)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_SWORD);
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        super.configure(section, directory);

        disarmNamedMobs = section.getBoolean("disarm-named-mobs", disarmNamedMobs);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(attacker instanceof Player playerAttacker) || !(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        if (!disarmNamedMobs && livingVictim.customName() != null) {
            return;
        }

        if (!EntityUtils.willCrit(playerAttacker)) {
            return;
        }

        EntityEquipment equipment = attacker.getEquipment();

        ItemStack item = equipment.getItemInMainHand();
        if (isEnchantmentOn(item)) {
            if (!WbsMath.chance(getLevel(item) * CHANCE_PER_LEVEL)) {
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
}
