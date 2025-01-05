package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;

public class FrenziedEnchant extends WbsEnchantment implements DamageEnchant {
    private static final int BASE_DURATION = 5 * 20;
    private static final int AMPLIFIER_INCREMENT = 2;

    public static final String DEFAULT_DESCRIPTION = "When you kill an enemy, you gain increased attack speed for "
            + BASE_DURATION / 20 + " seconds. If your " +
            "speed buff is already present, the speed will keep increasing every kill until the time left goes " +
            "below " + BASE_DURATION / 20 + " seconds.";

    public FrenziedEnchant() {
        super("frenzied", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON)
                .weight(5)
                .targetDescription("Shovel");
    }


    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(attacker instanceof Player playerAttacker) || !(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        EntityEquipment equipment = attacker.getEquipment();

        ItemStack item = equipment.getItemInMainHand();
        if (isEnchantmentOn(item)) {
            if (event.getFinalDamage() > livingVictim.getHealth()) {
                PotionEffect hasteEffect = playerAttacker.getPotionEffect(PotionEffectType.HASTE);

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

                playerAttacker.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, duration, amplifier));
            }
        }
    }
}
