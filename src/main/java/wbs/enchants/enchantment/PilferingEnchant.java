package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.utils.util.WbsMath;

import java.util.List;
import java.util.Random;

public class PilferingEnchant extends WbsEnchantment implements DamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "Devised by the illagers, this enchantment allows you to shake " +
            "down villagers to steal their trades without paying!";
    private static final int DROP_CHANCE_PER_LEVEL = 5;

    private static final NamespacedKey TIMES_HIT = new NamespacedKey("wbsenchants", "times_pilfered");

    public PilferingEnchant() {
        super("pilfering", DEFAULT_DESCRIPTION);

        maxLevel = 3;
        supportedItems = ItemTypeTagKeys.ENCHANTABLE_WEAPON;
        // TODO: exclusiveWith = Create set for Looting
        weight = 10;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Pilfering";
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(victim instanceof Villager villager)) {
            return;
        }

        if (!villager.isAdult()) {
            if (attacker instanceof Player player) {
                sendActionBar("&wYou monster...", player);
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
        if (isEnchantmentOn(item)) {
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
                        sendMessage("&wThis villager seems to be out of things to steal...", player);
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
