package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.BlockEnchant;

public class EternalLightEnchant extends WbsEnchantment implements BlockEnchant {
    public EternalLightEnchant() {
        super("eternal_light");
        registerBlockEvents();
    }

    @Override
    public void afterPlace(BlockPlaceEvent event, ItemStack placedItem) {
        if (!(event.getBlock().getState() instanceof Beacon beacon)) {
            return;
        }

        
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {

    }

    @Override
    public @NotNull String getDescription() {
        return "A beacon enchantment that also allows it to keep a 5x5 chunk area loaded!";
    }

    @Override
    public String getDisplayName() {
        return "&7Eternal Light";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
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
    public boolean canEnchant(TileState state) {
        return state instanceof Beacon;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType() == Material.BEACON;
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Beacon";
    }
}
