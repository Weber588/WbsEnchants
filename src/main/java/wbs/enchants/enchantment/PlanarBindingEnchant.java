package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
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
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

public class PlanarBindingEnchant extends WbsEnchantment {
    /**
     * In seconds
     */
    public static final int TIME_PER_LEVEL = 3;

    public static final String STRING_KEY = "planar_binding";

    public static final NamespacedKey TIME_KEY = new NamespacedKey(WbsEnchants.getInstance(),  STRING_KEY + "/time_bound");
    public static final NamespacedKey LEVEL_KEY = new NamespacedKey(WbsEnchants.getInstance(), STRING_KEY + "/binding_level");

    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setAmount(1), Particle.EXPLOSION_LARGE);

    private static final WbsSoundGroup SOUND = new WbsSoundGroup();

    static {
        SOUND.addSound(new WbsSound(Sound.BLOCK_BEACON_DEACTIVATE, 2f, 0.75f));
    }

    public PlanarBindingEnchant() {
        super(STRING_KEY);
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

        if (containsEnchantment(item)) {
            PersistentDataContainer dataContainer = event.getEntity().getPersistentDataContainer();

            dataContainer.set(TIME_KEY, PersistentDataType.LONG, System.currentTimeMillis());
            dataContainer.set(LEVEL_KEY, PersistentDataType.INTEGER, getLevel(item));

            if (event.getEntity() instanceof Player player) {
                WbsEnchants.getInstance().sendActionBar("&5" + event.getEntity().getName() + " bound!", player);
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
            if (timestamp > System.currentTimeMillis() * (long) level * TIME_PER_LEVEL * 1000) {
                EFFECT.play(entity.getLocation());
                return true;
            } else {
                dataContainer.remove(TIME_KEY);
                dataContainer.remove(LEVEL_KEY);
            }
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return "&7Planar Binding";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.RARE;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public @NotNull String getDescription() {
        return "After hitting a mob, it is unable to teleport for " + TIME_PER_LEVEL + " seconds (per level).";
    }
}
