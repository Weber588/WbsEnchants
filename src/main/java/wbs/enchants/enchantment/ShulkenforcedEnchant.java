package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Tag;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.VehicleEnchant;

public class ShulkenforcedEnchant extends WbsEnchantment implements VehicleEnchant {
    public ShulkenforcedEnchant() {
        super("shulkenforced");
    }

    @Override
    public void afterPlace(EntityPlaceEvent event, ItemStack placedItem) {
        if (event.getEntity() instanceof Vehicle vehicle) {
            vehicle.setGravity(false);
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "Vehicles with this enchantment are imbued with the power of shulkers, granting them resistance " +
                "to gravity!";
    }

    @Override
    public String getDisplayName() {
        return "&7Shulkenforced";
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
        return entity instanceof Vehicle;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        if (Tag.ITEMS_BOATS.isTagged(itemStack.getType()) ||
                Tag.ITEMS_CHEST_BOATS.isTagged(itemStack.getType()))
        {
            return true;
        }

        return switch (itemStack.getType()) {
            case MINECART, CHEST_MINECART, FURNACE_MINECART, HOPPER_MINECART, TNT_MINECART, COMMAND_BLOCK_MINECART
                    -> true;
            default -> false;
        };
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Vehicles";
    }
}
