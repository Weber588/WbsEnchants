package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public class AncientCryEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Makes nearby hostile mobs randomly start targeting another hostile mob.";

    private static final int RANGE = 64;

    public AncientCryEnchant() {
        super("ancient_cry", DEFAULT_DESCRIPTION);

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
            item.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(30)
                    .cooldownGroup(WbsEnchantsBootstrap.createKey("ancient_cry"))
                    .build()
            );
            player.setCooldown(item, Ticks.TICKS_PER_SECOND * 30);

            Location particleLoc = player.getEyeLocation().add(WbsEntityUtil.getFacingVector(player, 0.5));
            World world = player.getWorld();

            for (int i = 0; i <= 15; i += 5) {
                world.spawnParticle(Particle.SHRIEK, particleLoc, 1, i);
            }

            world.playSound(particleLoc, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1, 1);

            List<Mob> inRange = new RadiusSelector<>(Mob.class)
                    .setRange(RANGE)
                    .setPredicate(mob -> mob instanceof Enemy && !(mob instanceof Creeper))
                    .select(player);

            for (Mob mob : inRange) {
                Optional<Mob> closest = inRange.stream()
                        .filter(Predicate.not(mob::equals))
                        .min(Comparator.comparingDouble(check -> check.getLocation().distanceSquared(mob.getLocation())));

                closest.ifPresent(mob::setTarget);
            }
        }
    }
}
