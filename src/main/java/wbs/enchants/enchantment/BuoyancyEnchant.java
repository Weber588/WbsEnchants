package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.utils.util.entities.WbsEntityUtil;

import java.util.List;

public class BuoyancyEnchant extends WbsEnchantment implements TickableEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Allows mobs being ridden to swim without kicking off their rider.";
    private static final @NotNull Vector UPWARDS_PUSH = new Vector(0, 0.1, 0);

    public BuoyancyEnchant() {
        super("buoyancy", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(ItemTypeKeys.SADDLE)
                .activeSlots(EquipmentSlotGroup.SADDLE);
    }

    @Override
    public int getTickFrequency() {
        return 1;
    }

    @Override
    public void onTickEquipped(LivingEntity owner) {
        List<Entity> passengers = owner.getPassengers();
        if (passengers.isEmpty()) {
            return;
        }

        Block middleBlock = WbsEntityUtil.getMiddleLocation(owner).getBlock();
        if (middleBlock.getType() == Material.WATER || middleBlock.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) {
            owner.setVelocity(owner.getVelocity().add(UPWARDS_PUSH));
        }

    }
}
