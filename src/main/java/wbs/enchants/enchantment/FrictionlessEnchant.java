package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.VehicleEnchant;

public class FrictionlessEnchant extends WbsEnchantment implements VehicleEnchant {
    public FrictionlessEnchant() {
        super("frictionless");
    }

    @Override
    public @NotNull String getDescription() {
        return "A minecart enchantment that increases its maximum speed, but does not help it get there.";
    }

    @Override
    public String getDisplayName() {
        return "&7Frictionless";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.COMMON;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        // Doesn't matter -- overriding this with canEnchant anyway
        return EnchantmentTarget.ALL;
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
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public @NotNull WbsEnchantment getThisEnchantment() {
        return this;
    }

    @Override
    public void afterPlace(EntityPlaceEvent event, ItemStack placedItem) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Minecart minecart)) {
            return;
        }

        int level = getLevel(placedItem);

        double defaultMax = minecart.getMaxSpeed();

        minecart.setMaxSpeed(defaultMax * (1 + level / 3.0));
    }

    @Override
    public boolean canEnchant(Entity entity) {
        return entity instanceof Minecart;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType() == Material.MINECART;
    }
}
