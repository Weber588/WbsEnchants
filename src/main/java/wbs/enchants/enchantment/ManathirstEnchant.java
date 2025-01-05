package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;

import java.util.*;

public class ManathirstEnchant extends WbsEnchantment {

    private static final Set<UUID> usersToRepair = new HashSet<>();
    private static int timerId = -1;

    private static void addToRepairTimer(UUID uuid) {
        usersToRepair.add(uuid);

        if (timerId == -1) {
            timerId = new BukkitRunnable() {
                @Override
                public void run() {
                    if (usersToRepair.isEmpty()) {
                        timerId = -1;
                        cancel();
                        return;
                    }

                    repairAll();
                }
            }.runTaskTimer(WbsEnchants.getInstance(), 20, 20).getTaskId();
        }
    }

    private static void repairAll() {
        Map<ItemStack, Player> toRepair = new HashMap<>();

        Set<UUID> toRemove = new HashSet<>();
        for (UUID uuid : usersToRepair) {
            toRemove.add(uuid); // Assume removal, remove from toRemove if any items found.

            if (!(Bukkit.getEntity(uuid) instanceof Player player)) {
                continue;
            }

            if (!player.isOnline() || player.getTotalExperience() < EnchantManager.MANATHIRST.xpPerDura) {
                continue;
            }

            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && EnchantManager.MANATHIRST.isEnchantmentOn(item)) {
                    if (!(item.getItemMeta() instanceof Damageable damageable)) {
                        continue;
                    }

                    if (damageable.getDamage() == 0) {
                        continue;
                    }

                    toRepair.put(item, player);
                    toRemove.remove(uuid);
                }
            }
        }

        usersToRepair.removeAll(toRemove);

        for (ItemStack item : toRepair.keySet()) {
            if (!(item.getItemMeta() instanceof Damageable damageable)) {
                continue;
            }

            if (damageable.getDamage() > 0) {
                Player player = toRepair.get(item);
                damageable.setDamage(damageable.getDamage() - 1);
                player.setTotalExperience(Math.max(0, player.getTotalExperience() - EnchantManager.MANATHIRST.xpPerDura));

                item.setItemMeta(damageable);
            }
        }
    }

    private static final String DEFAULT_DESCRIPTION = "An alternative to mending, items with this enchantment will " +
            "slowly drain your XP bar to repair itself. Items are repaired slower than mending, " +
            "but will continuously self-repair so long as you have XP, even if you haven't gained any recently!";

    private int xpPerDura = 3;

    public ManathirstEnchant() {
        super("manathirst", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_DURABILITY)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_SELF_REPAIRING);
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        super.configure(section, directory);

        xpPerDura = section.getInt("xp-per-durability", xpPerDura);
    }

    @Override
    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection section = super.buildConfigurationSection(baseFile);
        section.set("xp-per-durability", xpPerDura);
        return section;
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();

        if (isEnchantmentOn(item)) {
            addToRepairTimer(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        addToRepairTimer(event.getPlayer().getUniqueId());
    }
}
