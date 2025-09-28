package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityAirChangeEvent;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.enchants.enchantment.helper.WbsCurse;

public class CurseHypoxia extends WbsCurse implements TickableEnchant {
    public static final int HEIGHT_THRESHOLD = 100;
    private static final String DEFAULT_DESCRIPTION = "You start losing oxygen above y " + HEIGHT_THRESHOLD + ".";

    public static final double DAMAGE_AT_THRESHOLD = 1;
    public static final double DAMAGE_AT_WORLD_HEIGHT = 4;

    public CurseHypoxia() {
        super("hypoxia", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR);
    }

    @Override
    public int getTickFrequency() {
        return 4;
    }

    @EventHandler
    public void onAirChange(EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }

        int newAmount = event.getAmount();
        int currentAmount = livingEntity.getRemainingAir();
        if (newAmount <= currentAmount) {
            return;
        }

        if (getSumLevels(livingEntity) > 0) {
            if (livingEntity.getLocation().getY() > HEIGHT_THRESHOLD) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onTickEquipped(LivingEntity owner) {
        double playerY = owner.getLocation().getY();
        if (playerY < HEIGHT_THRESHOLD) {
            return;
        }

        World world = owner.getWorld();
        if (world.hasCeiling()) {
            return;
        }

        int remainingAir = owner.getRemainingAir();
        if (remainingAir > 0) {
            owner.setRemainingAir(remainingAir - 1);
        } else {
            int worldHeight = world.getMaxHeight();
            double fractionThroughRange = playerY / (worldHeight - HEIGHT_THRESHOLD);
            double damage = DAMAGE_AT_THRESHOLD + fractionThroughRange * (DAMAGE_AT_WORLD_HEIGHT - DAMAGE_AT_THRESHOLD);

            owner.damage(damage, DamageSource.builder(DamageType.DROWN).build());
        }
    }
}
