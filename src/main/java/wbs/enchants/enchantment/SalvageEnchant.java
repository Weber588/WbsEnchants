package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;

@SuppressWarnings("UnstableApiUsage")
public class SalvageEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "When your Elytra breaks, you gain Slow Falling.";
    public static final int SECONDS_TO_FALL = 60;

    public SalvageEnchant() {
        super("salvage", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.ELYTRA)
                .maxLevel(1)
                .weight(1)
                .anvilCost(4);
    }

    @EventHandler
    public void onStopGliding(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        ItemStack item = getHighestEnchanted(entity, check -> check.hasData(DataComponentTypes.GLIDER));

        if (item == null) {
            return;
        }

        if (!item.hasData(DataComponentTypes.GLIDER)) {
            return;
        }

        if (!entity.isGliding()) {
            return;
        }

        Integer damage = item.getData(DataComponentTypes.DAMAGE);
        Integer maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE);
        if (damage != null && maxDamage != null && damage + 2 >= maxDamage) {
            WbsEnchants.getInstance().runLater(() -> {
                if (!entity.isGliding() && (!(entity instanceof Player player) || player.isOnline())) {
                    PotionEffect potionEffect = new PotionEffect(
                            PotionEffectType.SLOW_FALLING,
                            SECONDS_TO_FALL * Ticks.TICKS_PER_SECOND,
                            0,
                            false,
                            false,
                            true
                    );

                    entity.addPotionEffect(potionEffect);

                    WbsEnchants.getInstance().runTimer(runnable -> {
                        if ((!(entity instanceof Player player) || player.isOnline()) && entity.isGliding() || entity.isOnGround()) {
                            entity.removePotionEffect(PotionEffectType.SLOW_FALLING);
                        }
                    }, Ticks.TICKS_PER_SECOND, Ticks.TICKS_PER_SECOND);
                }
            }, 1);
        }

    }
}
