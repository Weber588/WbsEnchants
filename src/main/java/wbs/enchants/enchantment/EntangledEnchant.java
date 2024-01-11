package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.PersistentLocationType;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.*;

public class EntangledEnchant extends WbsEnchantment {
    private static final Map<Location, Entanglement> ENTANGLEMENTS = new HashMap<>();
    private static int timerId = -1;

    private static final WbsParticleGroup ENTANGLED_EFFECT = new WbsParticleGroup().addEffect(
            new NormalParticleEffect().setXYZ(0).setAmount(10), Particle.SPELL_WITCH
    );

    private static void createEntanglement(Player player, Block entangledBlock, Location location) {
        Entanglement entanglement = new Entanglement(player.getUniqueId(), entangledBlock, System.currentTimeMillis());
        ENTANGLEMENTS.put(location, entanglement);

        startDisentangleTimer();
    }

    private static void startDisentangleTimer() {
        if (timerId != -1) {
            return;
        }

        timerId = new BukkitRunnable() {
            @Override
            public void run() {
                List<Location> toRemove = new LinkedList<>();
                for (Location location : ENTANGLEMENTS.keySet()) {
                    Entanglement entanglement = ENTANGLEMENTS.get(location);
                    if (entanglement.createdTimestamp + 1000 < System.currentTimeMillis()) {
                        toRemove.add(location);
                    }
                }

                toRemove.forEach(ENTANGLEMENTS::remove);

                if (ENTANGLEMENTS.isEmpty()) {
                    cancel();
                    timerId = -1;
                }
            }
        }.runTaskTimer(WbsEnchants.getInstance(), 20, 20).getTaskId();
    }


    public EntangledEnchant() {
        super("entangled");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShiftClick(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.isSneaking()) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (containsEnchantment(item)) {
            BlockState state = clicked.getState();
            if (!(state instanceof Container)) {
                return;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }

            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            if (dataContainer.get(getKey(), PersistentLocationType.INSTANCE) == null) {
                dataContainer.set(getKey(), PersistentLocationType.INSTANCE, clicked.getLocation());

                WbsEnchants.getInstance().sendActionBar("Tool entangled!", player);
            } else {
                dataContainer.remove(getKey());

                WbsEnchants.getInstance().sendActionBar("&wLink removed!", player);
            }

            item.setItemMeta(meta);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();
        Player player = event.getPlayer();

        Entanglement entanglement = ENTANGLEMENTS.get(broken.getLocation());
        if (entanglement != null) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (containsEnchantment(item)) {
            BlockState state = broken.getState();
            if (state instanceof Container) {
                return;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }

            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            Location entangledLocation = dataContainer.get(getKey(), PersistentLocationType.INSTANCE);

            if (entangledLocation == null) {
                WbsEnchants.getInstance().sendActionBar("&wSneak punch a chest to entangle your tool!", player);
                return;
            }

            Block entangledBlock = entangledLocation.getBlock();
            if (!(entangledBlock.getState() instanceof Container)) {
                WbsEnchants.getInstance().sendActionBar("&wEntangled container broken!", player);
                dataContainer.remove(getKey());
                return;
            }

            createEntanglement(player, entangledBlock, broken.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        Entanglement entanglement = ENTANGLEMENTS.get(event.getBlock().getLocation());
        if (entanglement == null) {
            return;
        }

        Player player = event.getPlayer();

        Block entangledBlock = entanglement.entangledBlock;
        if (!(entangledBlock.getState() instanceof Container container)) {
            WbsEnchants.getInstance().sendActionBar("&wEntangled block missing!", player);
            return;
        }

        String containerName = WbsEnums.toPrettyString(container.getType());

        for (Item item : event.getItems()) {
            ItemStack stack = item.getItemStack();

            HashMap<Integer, ItemStack> failedToAdd = container.getInventory().addItem(stack);
            ENTANGLED_EFFECT.play(item.getLocation());
            ENTANGLED_EFFECT.play(entangledBlock.getLocation().add(0.5, 1, 0.5));

            ItemStack failed = failedToAdd.get(0);
            if (failed != null) {
                WbsEnchants.getInstance().sendActionBar(containerName + " is full!", player);

                HashMap<Integer, ItemStack> failedToPlayer = player.getInventory().addItem(failed);

                ItemStack failedAgain = failedToPlayer.get(0);
                ItemStack finalStack;
                if (failedAgain != null) {
                    finalStack = failedAgain;
                } else {
                    finalStack = new ItemStack(Material.AIR);
                }
                item.setItemStack(finalStack);
            } else {
                item.setItemStack(new ItemStack(Material.AIR));
            }
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Entangled";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.VERY_RARE;
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean isTreasure() {
        return true;
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
        return "Crouch and punch a container (chest, barrel etc) to \"entangle\" your tool to it. Once entangled, " +
                "drops from blocks broken will go straight to that chest anywhere in the world, so long as it has " +
                "enough space for it.";
    }

    @Override
    public @NotNull Map<NamespacedKey, Double> getLootKeyChances() {
        Map<NamespacedKey, Double> tableChances = new HashMap<>();

        // TODO: Make these configurable in a config
        tableChances.put(LootTables.END_CITY_TREASURE.getKey(), 50.0);
        tableChances.put(NamespacedKey.fromString("stellarity:end_city/ship_treasure"), 65.0);
        tableChances.put(NamespacedKey.fromString("stellarity:end_city/top_tower"), 25.0);

        return tableChances;
    }

    private record Entanglement(UUID playerUUID, Block entangledBlock, Long createdTimestamp) { }
}
