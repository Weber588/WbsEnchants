package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sittable;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;

public class RecallEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Summons all of your (non-sitting) tamed mobs to you, if they're in loaded chunks.";
    public static final int AMOUNT_PER_BLOCK = 10;

    public RecallEnchant() {
        super("recall", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.GOAT_HORN)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_HORNS);
    }

    @EventHandler
    public void onPlayGoatHorn(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || player.hasCooldown(item)) {
            return;
        }

        if (isEnchantmentOn(item)) {
            NormalParticleEffect areaEffect = new NormalParticleEffect();
            WbsParticleEffect puffEffect = new NormalParticleEffect()
                    .setSpeed(0.15)
                    .setAmount(25);

            player.getWorld().getLivingEntities().stream()
                    .filter(entity -> entity instanceof Tameable)
                    .map(entity -> (Tameable) entity)
                    .filter(tameable -> player.getUniqueId().equals(tameable.getOwnerUniqueId()))
                    .filter(tameable -> {
                        if (tameable instanceof Sittable sittable) {
                            return !sittable.isSitting();
                        }
                        return true;
                    })
                    .forEach(tameable -> {
                        double height = tameable.getHeight();
                        double width = tameable.getWidth();
                        Location middleLoc = WbsEntityUtil.getMiddleLocation(tameable);
                        areaEffect.setXYZ(width)
                                .setY(height)
                                .setAmount((int) (width * width * height) * AMOUNT_PER_BLOCK)
                                .play(Particle.WITCH, middleLoc);
                        puffEffect.play(Particle.DRAGON_BREATH, middleLoc);
                        tameable.teleport(player);
                        areaEffect.setXYZ(width)
                                .setY(height)
                                .setAmount((int) (width * width * height) * AMOUNT_PER_BLOCK)
                                .play(Particle.WITCH, middleLoc);
                    });

            puffEffect.play(Particle.DRAGON_BREATH, player.getLocation());
        }
    }
}
