package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

public class HellborneEnchant extends WbsEnchantment {
    public HellborneEnchant() {
        super("hellborne");
    }

    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        if (event.getDuration() < 20) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack chestItem = equipment.getChestplate();

        if (chestItem != null && containsEnchantment(chestItem)) {
            int level = getLevel(chestItem);

            PotionEffect strengthEffect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, event.getDuration(), level - 1);
            PotionEffect fireResEffect = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, event.getDuration(), 0);
            living.addPotionEffect(strengthEffect);
            living.addPotionEffect(fireResEffect);
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Hellborne";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.VERY_RARE;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_TORSO;
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

    @Override
    public @NotNull String getDescription() {
        return "You have strength and fire resistance while on fire, with the strength level relating to the " +
                "level of the enchantment.";
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Chestplate";
    }
}
