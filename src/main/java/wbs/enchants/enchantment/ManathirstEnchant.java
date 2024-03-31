package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;

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

            if (!player.isOnline() || player.getTotalExperience() < EnchantsSettings.MANATHIRST.xpPerDura) {
                continue;
            }

            EntityEquipment equipment = player.getEquipment();
            if (equipment == null) {
                continue;
            }

            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && EnchantsSettings.MANATHIRST.containsEnchantment(item)) {
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
                player.setTotalExperience(player.getTotalExperience() - EnchantsSettings.MANATHIRST.xpPerDura);

                item.setItemMeta(damageable);
            }
        }
    }

    private int xpPerDura = 3;

    public ManathirstEnchant() {
        super("manathirst");
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();

        if (containsEnchantment(item)) {
            addToRepairTimer(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        addToRepairTimer(event.getPlayer().getUniqueId());
    }

    @Override
    public @NotNull String getDescription() {
        return "An alternative to mending, items with this enchantment will slowly drain your XP bar to repair itself. " +
                "Items are repaired slower than mending, but will continuously self-repair so long as you have " +
                "XP, even if you haven't gained any recently!";
    }

    @Override
    public String getDisplayName() {
        return "&7Manathirst";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.RARE;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
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
    public Set<Enchantment> getIndirectConflicts() {
        return Set.of(MENDING);
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
}
