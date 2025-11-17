package wbs.enchants;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.enchants.enchantment.helper.*;
import wbs.enchants.events.EnchantedBlockBreakEvent;
import wbs.enchants.events.EnchantedBlockPlaceEvent;
import wbs.enchants.statuseffects.StatusEffectManager;
import wbs.enchants.statuseffects.StatusEffectType;

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
    private final Set<ItemModificationEnchant> itemModificationEnchants = new HashSet<>();
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

            if (enchantment instanceof ItemModificationEnchant itemModificationEnchant) {
                itemModificationEnchants.add(itemModificationEnchant);
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnchantBlock(EnchantedBlockBreakEvent event) {
        tickingEnchantedBlocks.remove(event.getBlock());
    }

    @EventHandler
    public void onDrinkMilk(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() == Material.MILK_BUCKET) {
            StatusEffectManager.clearStatusEffects(event.getPlayer(), StatusEffectType.RemoveReason.MILK);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        StatusEffectManager.clearStatusEffects(event.getEntity(), StatusEffectType.RemoveReason.DEATH);
    }

    private void startTickingEnchants() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) {
                    cancel();
                }

                Set<LivingEntity> allLivingEntities = new HashSet<>();
                Bukkit.getWorlds().forEach(world -> allLivingEntities.addAll(world.getLivingEntities()));

                tickEnchants(allLivingEntities);
                tickItemModificationEnchants(allLivingEntities);
                allLivingEntities.forEach(StatusEffectManager::tick);
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private void tickEnchants(Set<LivingEntity> allLivingEntities) {
        Set<TickableEnchant> tickableEnchants = new HashSet<>();

        this.tickableEnchants.forEach(enchant -> {
            if (Bukkit.getCurrentTick() % enchant.getTickFrequency() == 0) {
                tickableEnchants.add(enchant);
            }
        });

        if (tickableEnchants.isEmpty()) {
            return;
        }

        tickingEnchantedBlocks.removeIf(block ->
                !block.getChunk().isLoaded() || !BlockEnchant.hasBlockEnchants(block)
        );

        tickableEnchants.forEach(enchant -> {
            if (enchant instanceof TickableBlockEnchant blockEnchant) {
                tickingEnchantedBlocks.forEach(blockEnchant::onTick);
            }
        });

        tickableEnchants.forEach(TickableEnchant::onGlobalTick);

        for (LivingEntity entity : allLivingEntities) {
            if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            HashSet<TickableEnchant> hasEquipped = new HashSet<>();
            HashSet<TickableEnchant> hasAnywhere = new HashSet<>();
            Table<TickableEnchant, ItemStack, EquipmentSlot> equipped = HashBasedTable.create();
            Table<TickableEnchant, ItemStack, Integer> anywhere = HashBasedTable.create();

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

                    for (TickableEnchant enchant : tickableEnchants) {
                        if (!enchant.getDefinition().isActiveInSlot(slot)) {
                            continue;
                        }

                        if (enchant.isEnchantmentOn(itemStack)) {
                            enchant.onTickEquipped(entity, itemStack, slot);
                            equipped.put(enchant, itemStack, slot);
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

                    for (TickableEnchant enchant : tickableEnchants) {
                        if (enchant.isEnchantmentOn(itemStack)) {
                            enchant.onTickItemStack(entity, itemStack, slot);
                            anywhere.put(enchant, itemStack, slot);
                            hasAnywhere.add(enchant);
                        }
                    }
                }
            }

            hasAnywhere.forEach(enchant -> enchant.onTickAny(entity));
            anywhere.rowKeySet().forEach(enchant -> enchant.onTickItemStack(entity, anywhere.row(enchant)));
            hasEquipped.forEach(enchant -> enchant.onTickEquipped(entity));
            equipped.rowKeySet().forEach(enchant -> enchant.onTickEquipped(entity, equipped.row(enchant)));
        }
    }

    private void tickItemModificationEnchants(Set<LivingEntity> allLivingEntities) {
        if (Bukkit.getCurrentTick() % 20 * 60 == 0) {
            for (LivingEntity entity : allLivingEntities) {
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

                        for (ItemModificationEnchant enchant : itemModificationEnchants) {
                            enchant.validateUpdateItem(itemStack);
                        }
                    }
                }

                if (entity instanceof InventoryHolder holder) {
                    ItemStack[] contents = holder.getInventory().getContents();
                    for (ItemStack itemStack : contents) {
                        if (itemStack == null) {
                            continue;
                        }

                        for (ItemModificationEnchant enchant : itemModificationEnchants) {
                            enchant.validateUpdateItem(itemStack);
                        }
                    }
                }
            }
        }
    }
}
