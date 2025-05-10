package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;

public class CautiousnessEnchant extends WbsEnchantment implements DamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "Prevents damaging your pets and baby passive mobs, and prevents sweeping edge " +
            "from damaging other players.";

    public CautiousnessEnchant() {
        super("cautiousness", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        ItemStack enchanted = getIfEnchanted(attacker);

        if (enchanted != null) {
            switch (victim) {
                case Tameable tameable -> {
                    AnimalTamer owner = tameable.getOwner();
                    if (owner != null && owner.equals(attacker)) {
                        event.setCancelled(true);
                    }
                }
                case Player ignored -> {
                    if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                        event.setCancelled(true);
                    }
                }
                default -> {}
            }
        }
    }
}
