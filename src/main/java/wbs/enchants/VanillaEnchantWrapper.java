package wbs.enchants;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class VanillaEnchantWrapper extends WbsEnchantment {
    public VanillaEnchantWrapper(Enchantment vanilla) {
        super(vanilla.getKey());
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public @NotNull String getDescription() {
        return null;
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return null;
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
    public int getMinModifiedCost(int i) {
        return 0;
    }

    @Override
    public int getMaxModifiedCost(int i) {
        return 0;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return null;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return null;
    }
}
