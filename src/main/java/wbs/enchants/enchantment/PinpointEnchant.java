package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.DamageEnchant;

public class PinpointEnchant extends WbsEnchantment implements DamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "Arrows like needles mean the damage is highly localised, " +
            "making it possible to hit the same entity with multiple arrows in quick succession, " +
            "or even at the same time.";

    private static final NamespacedKey PINPOINT_KEY = WbsEnchantsBootstrap.createKey("pinpoint");

    public PinpointEnchant() {
        super("pinpoint", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_BOW)
                .weight(5);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (projectile == null) {
            return;
        }
        if (!(victim instanceof LivingEntity hit)) {
            return;
        }

        PersistentDataContainer container = projectile.getPersistentDataContainer();
        Boolean isPinpoint = container.get(PINPOINT_KEY, PersistentDataType.BOOLEAN);

        // Unsure why "isPinpoint == true" doesn't allow null OR false to return true? Guess I need to study Java more lol
        if (isPinpoint != null && isPinpoint) {
            hit.setNoDamageTicks(0);
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        Entity shot = event.getProjectile();
        if (!(shot instanceof Projectile projectile)) {
            // Not sure why getProjectile() isn't... y'know. A projectile.
            return;
        }

        ItemStack item = event.getBow();
        if (item == null) {
            item = event.getConsumable();
            if (item == null) {
                return;
            }
        }

        if (isEnchantmentOn(item)) {
            PersistentDataContainer container = projectile.getPersistentDataContainer();

            container.set(PINPOINT_KEY, PersistentDataType.BOOLEAN, true);
        }
    }
}
