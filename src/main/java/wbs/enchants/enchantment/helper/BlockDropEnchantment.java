package wbs.enchants.enchantment.helper;

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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.util.EventUtils;
import wbs.utils.util.WbsItems;

import java.util.*;

public abstract class BlockDropEnchantment extends WbsEnchantment {
    private final Map<Location, MarkedLocation> marked = new HashMap<>();
    private int timerId = -1;
    private final EventPriority dropPriority;

    public BlockDropEnchantment(@NotNull String key, @NotNull String description) {
        super(key, description);

        this.dropPriority = EventPriority.NORMAL;
    }

    public BlockDropEnchantment(@NotNull String key, EnchantmentType type, @NotNull String displayName, @NotNull String description) {
        super(key, type, displayName, description);

        this.dropPriority = EventPriority.NORMAL;
    }

    public BlockDropEnchantment(@NotNull String key, @NotNull String description, EventPriority dropPriority) {
        super(key, description);

        this.dropPriority = dropPriority;
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        EventUtils.register(BlockBreakEvent.class, this::onBlockBreak, EventPriority.MONITOR);
        EventUtils.register(BlockDropItemEvent.class, this::onBlockDrop, dropPriority);
    }

    private void mark(Player player, Block toMark, Location location) {
        MarkedLocation marked = new MarkedLocation(player.getUniqueId(), toMark, System.currentTimeMillis());
        this.marked.put(location, marked);

        startCleanupTimer();
    }

    protected void unmark(MarkedLocation marked) {
        for (Map.Entry<Location, MarkedLocation> entry : this.marked.entrySet()) {
            if (entry.getValue().equals(marked)) {
                this.marked.remove(entry.getKey());
                break;
            }
        }
    }

    private void startCleanupTimer() {
        if (timerId != -1) {
            return;
        }

        timerId = new BukkitRunnable() {
            @Override
            public void run() {
                List<Location> toRemove = new LinkedList<>();
                for (Location location : marked.keySet()) {
                    MarkedLocation marked = BlockDropEnchantment.this.marked.get(location);
                    // Delay 2 ticks, just to be safe
                    if (marked.createdTimestamp + 100 < System.currentTimeMillis()) {
                        toRemove.add(location);
                    }
                }

                toRemove.forEach(marked::remove);

                if (marked.isEmpty()) {
                    cancel();
                    timerId = -1;
                }
            }
        }.runTaskTimer(WbsEnchants.getInstance(), 20, 20).getTaskId();
    }

    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();
        Player player = event.getPlayer();

        MarkedLocation marked = this.marked.get(broken.getLocation());
        if (marked != null) {
            return;
        }

        BlockState state = broken.getState();
        if (state instanceof Container) {
            return;
        }

        ItemStack item = getIfEnchanted(player);

        if (item == null) {
            return;
        }

        if (!WbsItems.isProperTool(broken, item)) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        mark(player, broken, broken.getLocation());
    }

    public void onBlockDrop(BlockDropItemEvent event) {
        MarkedLocation marked = this.marked.get(event.getBlock().getLocation());
        if (marked == null) {
            return;
        }

        apply(event, marked);
    }

    protected abstract void apply(BlockDropItemEvent event, MarkedLocation marked);

    protected record MarkedLocation(UUID playerUUID, Block block, Long createdTimestamp) {}
}
