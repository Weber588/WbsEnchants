package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.enchantment.helper.WbsCurse;
import wbs.enchants.util.EntityUtils;

public class CurseRebuking extends WbsCurse implements DamageEnchant {
    public static final int PERCENT_PER_LEVEL = 20;
    private static final String DEFAULT_DESCRIPTION = "A weapon curse that causes the wielder to take " +
            PERCENT_PER_LEVEL + "% of damage dealt per level.";

    public CurseRebuking() {
        super("rebuking", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON);
    }

    // We'll register manually, so we can force MONITOR priority
    @Override
    public boolean autoRegister() {
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    @Override
    public void onDamage(EntityDamageByEntityEvent event) {
        DamageEnchant.super.onDamage(event);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        ItemStack item = EntityUtils.getEnchantedFromSlot(attacker, this, EquipmentSlot.HAND);

        if (item != null) {
            double finalDamage = event.getFinalDamage();

            double rebukeDamage = finalDamage * getLevel(item) * PERCENT_PER_LEVEL / 100;

            if (rebukeDamage >= 1) {
                attacker.damage(rebukeDamage, DamageSource.builder(DamageType.MAGIC).withCausingEntity(attacker).build());
            }
        }
    }
}
