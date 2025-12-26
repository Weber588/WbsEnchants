package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

public class PlanarBindingEnchant extends WbsEnchantment {
    /**
     * In seconds
     */
    public static final int TIME_PER_LEVEL = 3;

    public static final String STRING_KEY = "planar_binding";

    public static final NamespacedKey TIME_KEY = WbsEnchantsBootstrap.createKey(STRING_KEY + "/expire_time");
    public static final NamespacedKey LEVEL_KEY = WbsEnchantsBootstrap.createKey(STRING_KEY + "/binding_level");

    private static final WbsParticleEffect RING_EFFECT = new RingParticleEffect().setRadius(0.75).setAmount(20);
    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(RING_EFFECT, Particle.WITCH)
            .addEffect(RING_EFFECT, Particle.REVERSE_PORTAL);

    private static final String DEFAULT_DESCRIPTION = "After hitting a mob, it is unable to teleport for " +
            TIME_PER_LEVEL + " seconds (per level).";

    public PlanarBindingEnchant() {
        super(STRING_KEY, DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON)
                .weight(5);
    }


    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity living)) {
            return;
        }

        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            return;
        }
        ItemStack item = equipment.getItemInMainHand();

        if (isEnchantmentOn(item)) {
            PersistentDataContainer dataContainer = event.getEntity().getPersistentDataContainer();

            int level = getLevel(item);
            dataContainer.set(TIME_KEY, PersistentDataType.LONG, System.currentTimeMillis()
                    + (long) level * TIME_PER_LEVEL * 1000);
            dataContainer.set(LEVEL_KEY, PersistentDataType.INTEGER, level);

            if (event.getDamager() instanceof Player attacker) {
                sendActionBar("&5" + event.getEntity().getName() + " bound!", attacker);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event.getEntity() instanceof Player) {
            // Handle this separately with bypass for commands
            return;
        }

        event.setCancelled(tryBlockTeleport(event.getEntity()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        switch (event.getCause()) {
            case COMMAND:
            case NETHER_PORTAL:
            case END_PORTAL:
            case SPECTATE:
            case END_GATEWAY:
            case DISMOUNT:
                return;
        }

        Player player = event.getPlayer();

        event.setCancelled(tryBlockTeleport(player));
    }

    private boolean tryBlockTeleport(Entity entity) {
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        Long timestamp = dataContainer.get(TIME_KEY, PersistentDataType.LONG);
        Integer level = dataContainer.get(LEVEL_KEY, PersistentDataType.INTEGER);

        if (timestamp != null && level != null) {
            if (timestamp > System.currentTimeMillis()) {
                EFFECT.play(WbsEntityUtil.getMiddleLocation(entity));
                return true;
            } else {
                dataContainer.remove(TIME_KEY);
                dataContainer.remove(LEVEL_KEY);
            }
        }
        return false;
    }
}
