package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

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
    public Rarity getRarity() {
        return Rarity.COMMON;
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
    public @NotNull String getTargetDescription() {
        return "Map";
    }
}
