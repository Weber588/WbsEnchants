package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;

public class HellborneEnchant extends WbsEnchantment {
    public HellborneEnchant() {
        super("hellborne");
    }

    @EventHandler
    public void onCombust(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack chestItem = equipment.getChestplate();

        if (chestItem != null && containsEnchantment(chestItem)) {
            int ticks = Math.max(20, living.getFireTicks());

            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
                event.setCancelled(true);
            }

            int level = getLevel(chestItem);
            PotionEffect strengthEffect = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, ticks, level - 1);
            living.addPotionEffect(strengthEffect);
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
        return "You have strength and immunity to fire tick damage, with strength level relating to the " +
                "level of the enchantment.";
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Chestplate";
    }

    @Override
    public @Nullable Double getAddToChance(LootTable table) {
        NamespacedKey key = table.getKey();
        if (!key.getNamespace().equalsIgnoreCase("incendium")) {
            return null;
        }

        if (key.getKey().contains("armor") || key.getKey().contains("chestplate")) {
            return 15.0;
        }
        return null;
    }
}
