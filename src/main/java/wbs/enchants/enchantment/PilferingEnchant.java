package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.utils.util.WbsMath;

import java.util.List;
import java.util.Random;

public class PilferingEnchant extends WbsEnchantment implements DamageEnchant {
    private static final int DROP_CHANCE_PER_LEVEL = 5;

    private static final NamespacedKey TIMES_HIT = new NamespacedKey(WbsEnchants.getInstance(), "times_pilfered");

    public PilferingEnchant() {
        super("pilfering");
    }

    @Override
    public @NotNull String getDescription() {
        return "Devised by the illagers, this enchantment allows you to shake down villagers to steal their trades " +
                "without paying!";
    }

    @Override
    public String getDisplayName() {
        return "&7Pilfering";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.UNCOMMON;
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
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return matches(enchantment, LOOT_BONUS_MOBS);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(victim instanceof Villager villager)) {
            return;
        }

        if (!villager.isAdult()) {
            if (attacker instanceof Player player) {
                WbsEnchants.getInstance().sendActionBar("&wYou monster...", player);
            }
            return;
        }

        List<MerchantRecipe> trades = villager.getRecipes();
        if (trades.isEmpty()) {
            return;
        }

        EntityEquipment equipment = attacker.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack item = equipment.getItemInMainHand();
        if (containsEnchantment(item)) {
            int level = getLevel(item);

            if (!WbsMath.chance(DROP_CHANCE_PER_LEVEL * level)) {
                return;
            }

            PersistentDataContainer container = villager.getPersistentDataContainer();
            Integer timesHit = null;
            if (villager.getHealth() - event.getDamage() > 0 && container.has(TIMES_HIT)) {
                timesHit = container.get(TIMES_HIT, PersistentDataType.INTEGER);
                if (timesHit != null && timesHit >= level) {
                    if (attacker instanceof Player player) {
                        WbsEnchants.getInstance() 
                                .sendMessage("&wThis villager seems to be out of things to steal...", player);
                    }
                    return;
                }
            }

            int updatedTimesHit = timesHit == null ? 0 : timesHit;
            updatedTimesHit++;

            container.set(TIMES_HIT, PersistentDataType.INTEGER, updatedTimesHit);

            MerchantRecipe trade = trades.get(new Random().nextInt(trades.size()));

            victim.getWorld().dropItemNaturally(villager.getEyeLocation(), trade.getResult());
        }
    }
}
