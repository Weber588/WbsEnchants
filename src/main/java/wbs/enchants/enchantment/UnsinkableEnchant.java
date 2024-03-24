package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.VehicleEnchant;

import java.util.List;

public class UnsinkableEnchant extends WbsEnchantment implements VehicleEnchant {
    public UnsinkableEnchant() {
        super("unsinkable");
    }

    @EventHandler
    public void onBoatMove(VehicleMoveEvent event) {
        if (!(event.getVehicle() instanceof Boat boat)) {
            return;
        }

        if (!isInWater(boat)) {
            return;
        }

        Integer level = getLevel(boat);
        if (level != null) {
            Vector velocity = boat.getVelocity();
            velocity.setY(Math.max(velocity.getY() + 0.005 * level, 0.4));
            boat.setVelocity(velocity);
        }
    }

    private boolean isInWater(Boat boat) {
        World world = boat.getWorld();
        Location center = boat.getBoundingBox().getCenter().toLocation(world);

        if (isWater(center.clone().add(0, 0.5, 0))) {
            return true;
        }

        return getCorners(boat.getBoundingBox())
                .stream()
                .map(corner -> corner.toLocation(world).add(0, 0.5, 0))
                .anyMatch(this::isWater);
    }

    /*
    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getDismounted() instanceof Boat boat)) {
            return;
        }

        if (isEnchanted(boat)) {
            if (isInWater(boat)) {
                event.setCancelled(true);
            }
        }
    }
     */

    private List<Vector> getCorners(BoundingBox boundingBox) {
        double minX = boundingBox.getMinX();
        double minY = boundingBox.getMinY();
        double minZ = boundingBox.getMinZ();
        double maxX = boundingBox.getMaxX();
        double maxY = boundingBox.getMaxY();
        double maxZ = boundingBox.getMaxZ();

        return List.of(
                new Vector(minX, minY, minZ),
                new Vector(minX, maxY, minZ),

                new Vector(minX, minY, maxZ),
                new Vector(minX, maxY, maxZ),

                new Vector(maxX, minY, minZ),
                new Vector(maxX, maxY, minZ),

                new Vector(maxX, minY, maxZ),
                new Vector(maxX, maxY, maxZ)
        );
    }

    private boolean isWater(Location location) {
        Block block = location.getBlock();
        if (block.getType() == Material.WATER) {
            return true;
        } else {
            return block.getBlockData() instanceof Waterlogged data && data.isWaterlogged();
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "&7Boats with this enchantment can't stay sunk underwater!";
    }

    @Override
    public String getDisplayName() {
        return "&7Unsinkable";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        // Overridden in canEnchantItem
        return EnchantmentTarget.TOOL;
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
    public @NotNull WbsEnchantment getThisEnchantment() {
        return this;
    }

    @Override
    public boolean canEnchant(Entity entity) {
        return entity instanceof Boat;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return Tag.ITEMS_BOATS.isTagged(itemStack.getType()) || Tag.ITEMS_CHEST_BOATS.isTagged(itemStack.getType());
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Boat";
    }
}
