package wbs.enchants;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.enchants.enchantment.helper.MovementEnchant;
import wbs.enchants.enchantment.helper.TickableBlockEnchant;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.enchants.events.EnchantedBlockPlaceEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A helper class that handles certain events and distributes them to registered enchantments, to share
 * logic and to avoid excessive processing during high-frequency events
 */
public class SharedEventHandler implements Listener {
    private final WbsEnchants plugin;

    private final Set<TickableEnchant> tickableEnchants = new HashSet<>();
    private final Set<MovementEnchant> movementEnchants = new HashSet<>();

    private final Set<Block> tickingEnchantedBlocks = new HashSet<>();

    public SharedEventHandler(WbsEnchants plugin) {
        this.plugin = plugin;
    }

    public void start() {
        loadSharedEventEnchants();

        startTickingEnchants();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadSharedEventEnchants() {
        EnchantManager.getCustomRegistered().forEach(enchantment -> {
            if (enchantment instanceof TickableEnchant tickable) {
                tickableEnchants.add(tickable);
            }

            if (enchantment instanceof MovementEnchant movementEnchant) {
                movementEnchants.add(movementEnchant);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMoveToNewBlock(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlock().equals(to.getBlock())) {
            return;
        }

        movementEnchants.forEach(enchant -> enchant.onChangeBlock(event.getPlayer(), from.getBlock(), to.getBlock()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(ChunkLoadEvent event) {
        Map<Block, Map<BlockEnchant, Integer>> enchantedBlocks = BlockEnchant.getBlockEnchantments(event.getChunk());

        tickingEnchantedBlocks.addAll(enchantedBlocks.keySet());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Map<Block, Map<BlockEnchant, Integer>> enchantedBlocks = BlockEnchant.getBlockEnchantments(event.getChunk());

        tickingEnchantedBlocks.removeAll(enchantedBlocks.keySet());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnchantBlock(EnchantedBlockPlaceEvent event) {
        tickingEnchantedBlocks.add(event.getBlock());
    }

    private void startTickingEnchants() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    cancel();
                }

                Set<TickableEnchant> toTick = new HashSet<>();

                tickableEnchants.forEach(enchant -> {
                    if (Bukkit.getCurrentTick() % enchant.getTickFrequency() == 0) {
                        toTick.add(enchant);
                    }
                });

                if (toTick.isEmpty()) {
                    return;
                }

                tickingEnchantedBlocks.removeIf(block -> !block.getChunk().isLoaded());

                toTick.forEach(enchant -> {
                    if (enchant instanceof TickableBlockEnchant blockEnchant) {
                        tickingEnchantedBlocks.forEach(blockEnchant::onTick);
                    }
                });

                toTick.forEach(TickableEnchant::onGlobalTick);

                Set<LivingEntity> allLivingEntities = new HashSet<>();
                Bukkit.getWorlds().forEach(world -> allLivingEntities.addAll(world.getLivingEntities()));

                for (LivingEntity entity : allLivingEntities) {
                    HashSet<TickableEnchant> hasEquipped = new HashSet<>();
                    HashSet<TickableEnchant> hasAnywhere = new HashSet<>();

                    EntityEquipment equipment = entity.getEquipment();
                    if (equipment != null) {
                        for (EquipmentSlot slot : EquipmentSlot.values()) {
                            if (!entity.canUseEquipmentSlot(slot)) {
                                continue;
                            }

                            ItemStack itemStack = equipment.getItem(slot);

                            if (itemStack.isEmpty()) {
                                continue;
                            }

                            for (TickableEnchant enchant : toTick) {
                                if (!enchant.getDefinition().isActiveInSlot(slot)) {
                                    continue;
                                }

                                if (enchant.isEnchantmentOn(itemStack)) {
                                    enchant.onTickEquipped(entity, itemStack, slot);
                                    hasEquipped.add(enchant);
                                }
                            }
                        }
                    }

                    if (entity instanceof InventoryHolder holder) {
                        ItemStack[] contents = holder.getInventory().getContents();
                        for (int slot = 0; slot < contents.length; slot++) {
                            ItemStack itemStack = contents[slot];

                            if (itemStack == null) {
                                continue;
                            }

                            for (TickableEnchant enchant : toTick) {
                                if (enchant.isEnchantmentOn(itemStack)) {
                                    enchant.onTickItemStack(entity, itemStack, slot);
                                    hasAnywhere.add(enchant);
                                }
                            }
                        }
                    }

                    hasAnywhere.forEach(enchant -> enchant.onTickAny(entity));
                    hasEquipped.forEach(enchant -> enchant.onTickEquipped(entity));
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }
}
