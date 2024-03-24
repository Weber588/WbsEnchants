package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.DamageUtils;
import wbs.utils.util.WbsMath;

public class HellborneEnchant extends WbsEnchantment {
    public HellborneEnchant() {
        super("hellborne");
    }

    @EventHandler
    public void onCombust(EntityDamageEvent event) {
        if (!DamageUtils.isHeat(event.getCause())) {
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
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
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
        return true;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public @NotNull String getDescription() {
        return "You have strength and immunity to fire tick damage, with strength level relating to the " +
                "level of the enchantment while you're on fire.";
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Chestplate";
    }

    @Override
    public void onLootGenerate(LootGenerateEvent event) {
        if (WbsMath.chance(10)) {
            Location location = event.getLootContext().getLocation();
            World world = location.getWorld();
            if (world == null) {
                return;
            }
            if (world.getEnvironment() == World.Environment.NETHER) {
                for (ItemStack stack : event.getLoot()) {
                    if (tryAdd(stack, 1)) {
                        return;
                    }
                }
            }
        }
    }
}
