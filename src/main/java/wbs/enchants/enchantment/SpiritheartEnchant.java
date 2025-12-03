package wbs.enchants.enchantment;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.TickableEnchant;

public class SpiritheartEnchant extends WbsEnchantment implements TickableEnchant {
    private static final int TICKS_PER_DAY = 1200 * Ticks.TICKS_PER_SECOND;
    private static final NamespacedKey LAST_APPLIED_KEY = WbsEnchantsBootstrap.createKey("spiritheart_last_applied");

    private static int getTimeUntilMidnight() {
        long mainWorldTimestamp = Bukkit.getWorlds().getFirst().getFullTime();

        long ticksSinceStartOfDay = (mainWorldTimestamp % TICKS_PER_DAY);

        long ticksUntilDawn = (TICKS_PER_DAY - ticksSinceStartOfDay);
        long ticksUntilMidnight = ticksUntilDawn - (TICKS_PER_DAY / 4);
        if (ticksUntilMidnight < 0) {
            ticksUntilMidnight += TICKS_PER_DAY;
        }

        return (int) ticksUntilMidnight;
    }

    public static final int POTION_LEVEL_PER_LEVEL = 1;
    public static final String DESCRIPTION = "You have " + (POTION_LEVEL_PER_LEVEL * 2) + " absorption hearts per " +
            "level that reset at midnight.";
    public SpiritheartEnchant() {
        super("spiritheart", DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_CHEST_ARMOR)
                .maxLevel(3);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
    }

    public void applyFor(LivingEntity entity, ItemStack enchanted) {
        PersistentDataContainer container = entity.getPersistentDataContainer();

        Integer lastAppliedTick = container.get(LAST_APPLIED_KEY, PersistentDataType.INTEGER);
        // If they've never had it, or if they last got it more than 1 day ago (or in the first second of the day), give it again
        if (lastAppliedTick == null || lastAppliedTick < Bukkit.getCurrentTick() - TICKS_PER_DAY + 20) {
            int level = getLevel(enchanted);

            PotionEffect effect = new PotionEffect(PotionEffectType.ABSORPTION,
                    getTimeUntilMidnight(),
                    level * POTION_LEVEL_PER_LEVEL,
                    true,
                    false,
                    false
            );

            effect.apply(entity);

            container.set(LAST_APPLIED_KEY, PersistentDataType.INTEGER, Bukkit.getCurrentTick());
        }
    }

    public void onPassedMidnight() {
        for (World world : Bukkit.getWorlds()) {
            for (LivingEntity entity : world.getLivingEntities()) {
                ItemStack highestEnchanted = getHighestEnchantedArmour(entity);
                if (highestEnchanted != null) {
                    applyFor(entity, highestEnchanted);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerArmourChangeDirect(PlayerArmorChangeEvent event) {
        ItemStack newItem = event.getNewItem();
        ItemStack oldItem = event.getOldItem();

        Player player = event.getPlayer();

        if (isEnchantmentOn(oldItem)) {
            player.removePotionEffect(PotionEffectType.ABSORPTION);
        }

        if (isEnchantmentOn(newItem)) {
            applyFor(player, newItem);
        }
    }

    @EventHandler
    public void onSpawnWithArmour(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        ItemStack highestEnchanted = getHighestEnchantedArmour(entity);

        if (highestEnchanted != null) {
            applyFor(entity, highestEnchanted);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        LivingEntity entity = event.getPlayer();

        ItemStack highestEnchanted = getHighestEnchantedArmour(entity);

        if (highestEnchanted != null) {
            applyFor(entity, highestEnchanted);
        }
    }

    @EventHandler
    public void onDispenseArmour(BlockDispenseArmorEvent event) {
        LivingEntity entity = event.getTargetEntity();

        if (isEnchantmentOn(event.getItem())) {
            applyFor(entity, event.getItem());
        }
    }

    @Override
    public void onGlobalTick() {
        WbsEnchants.getInstance().runLater(this::onPassedMidnight, getTimeUntilMidnight());
    }

    @Override
    public int getTickFrequency() {
        return TICKS_PER_DAY;
    }
}
