package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;

public class EagleEyedEnchant extends WbsEnchantment implements DamageEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "When hit by a projectile, the shooter glows.";

    public EagleEyedEnchant() {
        super("eagle_eyed", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (projectile == null) {
            return;
        }

        if (!(victim instanceof Player playerVictim)) {
            return;
        }

        if (getSumLevels(playerVictim) > 0) {
            playerVictim.sendPotionEffectChange(attacker, new PotionEffect(PotionEffectType.GLOWING, Ticks.TICKS_PER_SECOND * 5, 0));
        }
    }
}
