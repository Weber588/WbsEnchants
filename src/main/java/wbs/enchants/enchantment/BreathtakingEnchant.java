package wbs.enchants.enchantment;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;

public class BreathtakingEnchant extends WbsEnchantment implements DamageEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Takes away oxygen when you hit an entity";

    public BreathtakingEnchant() {
        super("breathtaking", DEFAULT_DESCRIPTION);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        ItemStack item = getIfEnchanted(attacker);
        if (item != null) {
            livingVictim.setRemainingAir(livingVictim.getRemainingAir() - livingVictim.getMaximumAir() / 2);
        }
    }
}
