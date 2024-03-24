package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.NonPersistentBlockEnchant;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.WbsMath;

public class AridityEnchant extends WbsEnchantment implements NonPersistentBlockEnchant {
    // TODO: Make these configurable
    private static final int MAX_LEVEL = 3;
    private static final int CHANCE_PER_LEVEL = 100 / (MAX_LEVEL + 1);

    public AridityEnchant() {
        super("aridity");
        registerNonPersistentBlockEvents();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDryFurnace(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();
        if (containsEnchantment(source)) {
            ItemStack result = event.getResult();

            ItemStack newResult = source.clone();
            newResult.setType(result.getType());
            newResult.setAmount(result.getAmount());

            event.setResult(newResult);
        }
    }

    @Override
    public @NotNull String getDescription() {
        String description = "A sponge enchantment that automatically returns it to your hand after placing";
        if (MAX_LEVEL >= 0) {
            description += ", with a " +
            CHANCE_PER_LEVEL + "% chance of auto-drying immediately, for a maximum chance of " +
                    (CHANCE_PER_LEVEL * MAX_LEVEL) + "% chance at level " + MAX_LEVEL;
        } else {
            description += ".";
        }

        return description;
    }

    @Override
    public String getDisplayName() {
        return "&7Aridity";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return MAX_LEVEL;
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
    public void onPlace(BlockPlaceEvent event, ItemStack placedItem) {
        // Not cancelling - let the sponge do its thing, and then just give it back to the player (dried or otherwise)

        Player player = event.getPlayer();

        ItemStack cloned = placedItem.clone();
        cloned.setAmount(1);

        Block block = event.getBlock();
        boolean inNether = block.getWorld().getEnvironment() == World.Environment.NETHER;

        if (inNether) {
            cloned.setType(Material.SPONGE);
        } else if (cloned.getType() == Material.SPONGE && !WbsMath.chance(CHANCE_PER_LEVEL * getLevel(placedItem))) {
            int spongeRange = 7;
            rangeLoop: for (int x = -spongeRange; x < spongeRange; x++) {
                for (int y = -spongeRange; y < spongeRange; y++) {
                    for (int z = -spongeRange; z < spongeRange; z++) {
                        if (Math.abs(x) + Math.abs(y) + Math.abs(z) <= spongeRange) {
                            if (block.getLocation().clone().add(x, y, z).getBlock().getType() == Material.WATER) {
                                cloned.setType(Material.WET_SPONGE);
                                break rangeLoop;
                            }
                        }
                    }
                }
            }
        }

        // Wait until next tick
        plugin.runSync(() -> {
            EntityUtils.giveSafely(player, cloned);

            event.getBlock().setType(Material.AIR);
        });
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getType() == Material.SPONGE ||
                block.getType() == Material.WET_SPONGE;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType() == Material.SPONGE ||
                itemStack.getType() == Material.WET_SPONGE;
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Sponge";
    }
}
