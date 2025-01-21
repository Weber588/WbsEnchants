package wbs.enchants.enchantment.shulkerbox;

import io.papermc.paper.block.TileStateInventoryHolder;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.ShulkerBoxEnchantment;
import wbs.enchants.enchantment.helper.TickableBlockEnchant;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;

public class DiscardingEnchant extends ShulkerBoxEnchantment implements TickableBlockEnchant {
    private static final WbsParticleEffect EFFECT = new NormalParticleEffect()
            .setXYZ(0.3)
            .setAmount(20);

    private static final String DEFAULT_DESCRIPTION = "Deletes any items that enter it.";

    public DiscardingEnchant() {
        super("discarding", DEFAULT_DESCRIPTION);

        getDefinition()
                .exclusiveWith(EnchantManager.CARRYING)
                .type(EnchantmentTypeManager.PARADOXICAL);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryInteract(InventoryEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof TileStateInventoryHolder holder) {
            Block block = holder.getBlock();
            Integer level = getLevel(block);
            if (level != null) {
                inventory.clear();
                holder.update();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        Inventory receiver = event.getDestination();
        if (receiver.getHolder() instanceof TileStateInventoryHolder holder) {
            Block block = holder.getBlock();
            Integer level = getLevel(block);
            if (level != null) {
                // This event happens before the item is transferred, and if we change it to air it'll
                // not be removed from the source inventory.
                WbsEnchants.getInstance().runSync(() -> {
                    receiver.clear();
                    holder.update();
                });
            }
        }
    }

    // Just to make a spooky sound play when opening it so it's obvious it's gonna do smth weird
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryInteract(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof TileStateInventoryHolder holder) {
            Block block = holder.getBlock();
            Integer level = getLevel(block);
            if (level != null) {
                block.getWorld().playSound(block.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 0.5f, 0.5f);
            }
        }
    }

    @Override
    public void onTick(Block block) {
        EFFECT.play(Particle.SMOKE, block.getLocation().add(0.5, 0.5, 0.5));
    }

    @Override
    public int getTickFrequency() {
        return 5;
    }
}
