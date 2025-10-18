package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.LodestoneTracker;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.utils.util.persistent.WbsPersistentDataType;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class TrackingEnchant extends WbsEnchantment implements TickableEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Allows you to right click an entity to track it with the compass.";

    public TrackingEnchant() {
        super("tracking", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.COMPASS)
                .maxLevel(3);
    }

    @Override
    public int getTickFrequency() {
        return Ticks.TICKS_PER_SECOND * 15;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void rightClickEntity(PlayerInteractEntityEvent event) {
        EquipmentSlot hand = event.getHand();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(hand);

        if (!isEnchantmentOn(item)) {
            return;
        }

        item.editPersistentDataContainer(container -> {
            container.set(getKey(), WbsPersistentDataType.UUID, event.getRightClicked().getUniqueId());
        });
        event.setCancelled(true);
    }

    @Override
    public void onTickItemStack(LivingEntity owner, ItemStack item, int slot) {
        if (!isEnchantmentOn(item)) {
            return;
        }

        // Only fire every (enchantment level) period
        if (Bukkit.getCurrentTick() % (getTickFrequency() * (getDefinition().maxLevel() - getLevel(item) + 1)) != 0) {
            return;
        }

        UUID uuid = item.getPersistentDataContainer().get(getKey(), WbsPersistentDataType.UUID);
        Location location = null;
        boolean isPlayerUUID = false;
        if (uuid != null) {
            Entity entity = Bukkit.getEntity(uuid);

            if (entity != null) {
                location = entity.getLocation();
            }

            isPlayerUUID = Bukkit.getOfflinePlayer(uuid).getFirstPlayed() > 0;
            
            // If the entity is dead/invalid, remove the uuid (unless it's a player -- they'll respawn)
            if (entity != null && (entity.isDead() || !entity.isValid()) && !isPlayerUUID) {
                item.editPersistentDataContainer(container -> {
                    container.remove(getKey());
                });
            }
        }

        // Update if the location was found, OR if it has no lodestone component (unless it's a player -- use last known location then)
        if (location != null || (!isPlayerUUID && !item.hasData(DataComponentTypes.LODESTONE_TRACKER))) {
            item.setData(DataComponentTypes.LODESTONE_TRACKER, LodestoneTracker.lodestoneTracker()
                    .location(location)
                    .tracked(false)
                    .build());
        }
    }
}
