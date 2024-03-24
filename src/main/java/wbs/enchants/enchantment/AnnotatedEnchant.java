package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

import java.util.Set;

public class AnnotatedEnchant extends WbsEnchantment {
    public AnnotatedEnchant() {
        super("annotated");
    }

    @EventHandler
    public void onMapRender(MapInitializeEvent event) {

    }

    @Override
    public @NotNull String getDescription() {
        return "Maps with this enchantment will show all structures it can, even if you haven't explored them yet!";
    }

    @Override
    public String getDisplayName() {
        return "&7Annotated";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.COMMON;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return null;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        // Overriden in canEnchant
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
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType() == Material.FILLED_MAP;
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
    public @NotNull String getTargetDescription() {
        return "Map";
    }
}
