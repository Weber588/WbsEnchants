package wbs.enchants.enchantment.curse;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EntityUtils;

import java.util.Set;

public class CurseStumbling extends WbsEnchantment {
    public CurseStumbling() {
        super("curse_stumbling");
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        ItemStack boots = EntityUtils.getEnchantedFromSlot(entity, this, EquipmentSlot.FEET);

        if (boots != null) {
            double damage = event.getDamage();
            damage *= (1 + (0.12 * getLevel(boots)));
            event.setDamage(damage);
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "A curse that causes increased fall damage when worn on boots; essentially the opposite of Feather Falling.";
    }

    @Override
    public String getDisplayName() {
        return "&cCurse of Stumbling";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_FEET;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @Override
    public Set<Enchantment> getDirectConflicts() {
        return Set.of(PROTECTION_FALL);
    }
}
