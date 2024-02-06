package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.utils.util.WbsMath;

import java.util.Random;

public class FrenziedEnchant extends WbsEnchantment implements DamageEnchant {
    private static final int BASE_DURATION = 5 * 20;
    private static final int AMPLIFIER_INCREMENT = 2;

    public FrenziedEnchant() {
        super("frenzied");
    }


    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(attacker instanceof Player playerAttacker) || !(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        EntityEquipment equipment = attacker.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack item = equipment.getItemInMainHand();
        if (containsEnchantment(item)) {
            if (event.getFinalDamage() > livingVictim.getHealth()) {
                PotionEffect hasteEffect = playerAttacker.getPotionEffect(PotionEffectType.FAST_DIGGING);

                int amplifier = AMPLIFIER_INCREMENT;
                int duration = BASE_DURATION;
                if (hasteEffect != null) {
                    amplifier = hasteEffect.getAmplifier();
                    duration = hasteEffect.getDuration();
                }

                if (duration < BASE_DURATION) {
                    duration += BASE_DURATION;
                } else if (duration > BASE_DURATION) {
                    amplifier += AMPLIFIER_INCREMENT;
                }

                playerAttacker.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, amplifier));
            }
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Frenzied";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.RARE;
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
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public @NotNull String getDescription() {
        double defaultDuration = (double) BASE_DURATION / 20;
        return "When you kill an enemy, you gain increased attack speed for " + defaultDuration + " seconds. If your " +
                "speed buff is already present, the speed will keep increasing every kill until the time left goes " +
                "below " + defaultDuration + " seconds.";
    }

    @Override
    public void onLootGenerate(LootGenerateEvent event) {
        if (WbsMath.chance(20)) {
            Location location = event.getLootContext().getLocation();
            World world = location.getWorld();
            if (world == null) {
                return;
            }
            if (world.getEnvironment() == World.Environment.NORMAL) {
                for (ItemStack stack : event.getLoot()) {
                    if (tryAdd(stack, new Random().nextInt(2) + 1)) {
                        return;
                    }
                }
            }
        }
    }
}
