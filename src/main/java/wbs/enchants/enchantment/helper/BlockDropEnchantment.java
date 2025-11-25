package wbs.enchants.enchantment.helper;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.EventUtils;
import wbs.utils.util.WbsItems;

import java.util.*;

public interface BlockDropEnchantment extends EnchantInterface, AutoRegistrableEnchant {
    Table<Location, BlockDropEnchantment, MarkedLocation> PENDING_DROPS = HashBasedTable.create();

    static void removePendingDrop(MarkedLocation marked) {
        PENDING_DROPS.remove(marked.block.getLocation(), marked.enchantment);
    }

    static void addPendingDrop(MarkedLocation marked) {
        PENDING_DROPS.put(marked.block.getLocation(), marked.enchantment, marked);
    }

    default EventPriority getDropPriority() {
        return EventPriority.NORMAL;
    }

    default void registerBlockDropEvents() {
        EventUtils.register(BlockBreakEvent.class, this::onBlockBreak, EventPriority.MONITOR);
        EventUtils.register(BlockDropItemEvent.class, this::onBlockDrop, getDropPriority());
    }

    private void mark(Player player, Block toMark) {
        MarkedLocation marked = new MarkedLocation(player.getUniqueId(), toMark, System.currentTimeMillis(), this);
        addPendingDrop(marked);

        WbsEnchants.getInstance().runSync(() -> {
            removePendingDrop(marked);
        });
    }

    default MarkedLocation getPendingDrop(Location location) {
        return PENDING_DROPS.get(location, this);
    }

    default void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();
        Player player = event.getPlayer();

        MarkedLocation marked = getPendingDrop(broken.getLocation());
        if (marked != null) {
            return;
        }

        BlockState state = broken.getState();
        if (state instanceof Container) {
            return;
        }

        ItemStack item = getThisEnchantment().getIfEnchanted(player);

        if (item == null) {
            return;
        }

        if (!allowIncorrectTools() && !WbsItems.isProperTool(broken, item)) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        mark(player, broken);
    }

    default void onBlockDrop(BlockDropItemEvent event) {
        MarkedLocation marked = getPendingDrop(event.getBlock().getLocation());
        if (marked == null) {
            return;
        }

        apply(event, marked);
    }

    default boolean allowIncorrectTools() {
        return false;
    }

    void apply(BlockDropItemEvent event, MarkedLocation marked);

    record MarkedLocation(UUID playerUUID, Block block, Long createdTimestamp, BlockDropEnchantment enchantment) {}
}
