package wbs.enchants.enchantment;

import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.enchants.enchantment.helper.VehicleEnchant;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;

public class DiscardingEnchant extends WbsEnchantment implements BlockEnchant, VehicleEnchant {
    private static final WbsParticleEffect EFFECT = new NormalParticleEffect().setXYZ(0.7).setAmount(10);
    private static final String DEFAULT_DESCRIPTION = "Deletes any items picked up/transferred in, " +
            "but not ones that you put in directly.";

    public DiscardingEnchant() {
        super("discarding", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(
                        ItemTypeKeys.HOPPER,
                        ItemTypeKeys.HOPPER_MINECART
                ).exclusiveWith(EnchantManager.CARRYING)
                .type(EnchantmentTypeManager.PARADOXICAL);
    }

    private void removeIfEnchantedHolder(Inventory inventory, ItemStack item) {
        Integer level;

        ItemStack cloned = item.clone();
        WbsEnchants.getInstance().getLogger().info("inventory.getHolder(): " + inventory.getHolder());
        WbsEnchants.getInstance().getLogger().info("inventory.getClass(): " + inventory.getClass());
        WbsEnchants.getInstance().getLogger().info("inventory.getHolder().getClass(): " + inventory.getHolder().getClass());
        if (inventory.getHolder() instanceof TileStateInventoryHolder holder) {
            WbsEnchants.getInstance().getLogger().info("holder was tile state");
            Block block = holder.getBlock();
            level = getLevel(block);
            WbsEnchants.getInstance().getLogger().info("level: " + level);
            if (level != null) {
                WbsEnchants.getInstance().runSync(() -> {
                    WbsEnchants.getInstance().getLogger().info("next tick, checking updated state");
                    if (block.getState() instanceof TileStateInventoryHolder updatedHolder) {
                        WbsEnchants.getInstance().getLogger().info("same");
                        Integer newLevel = getLevel(block);
                        WbsEnchants.getInstance().getLogger().info("new level: " + newLevel);
                        if (newLevel != null) {
                            // Gotta use snapshot inventory because (for legacy reasons according to md5),
                            // getInventory is clobbered when update is called.
                            updatedHolder.getSnapshotInventory().removeItem(cloned);

                            updatedHolder.update();
                            EFFECT.play(Particle.SMOKE, block.getLocation());
                        }
                    }
                });
            }
        } else if (inventory.getHolder() instanceof HopperMinecart holder) {
            Entity entity = holder.getEntity();
            level = getLevel(entity);
            if (level != null) {
                WbsEnchants.getInstance().runSync(() -> {
                    if (Bukkit.getEntity(entity.getUniqueId()) instanceof HopperMinecart updatedHolder) {
                        if (updatedHolder.isValid()) {
                            Integer newLevel = getLevel(updatedHolder);
                            if (newLevel != null) {
                                updatedHolder.getInventory().remove(cloned);
                                EFFECT.play(Particle.SMOKE, WbsEntityUtil.getMiddleLocation(updatedHolder));
                            }
                        }
                    }
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        Inventory receiver = event.getDestination();
        removeIfEnchantedHolder(receiver, event.getItem());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        removeIfEnchantedHolder(event.getInventory(), event.getItem().getItemStack());
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getState() instanceof Hopper;
    }

    @Override
    public boolean canEnchant(Entity entity) {
        return entity.getType() == EntityType.HOPPER_MINECART;
    }
}
