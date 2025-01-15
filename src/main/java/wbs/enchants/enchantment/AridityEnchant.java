package wbs.enchants.enchantment;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.NonPersistentBlockEnchant;
import wbs.enchants.util.EntityUtils;

public class AridityEnchant extends WbsEnchantment implements NonPersistentBlockEnchant {
    private static final String DEFAULT_DESCRIPTION = "A sponge enchantment that automatically returns it to your " +
            "hand after placing, never getting wet!";

    public AridityEnchant() {
        super("aridity", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.SPONGES)
                .targetDescription("Sponge");
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getType() == Material.SPONGE || block.getType() == Material.WET_SPONGE;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDryFurnace(FurnaceSmeltEvent event) {
        ItemStack source = event.getSource();
        if (isEnchantmentOn(source)) {
            ItemStack result = event.getResult();

            result.setItemMeta(source.getItemMeta());
        }
    }

    @Override
    public void onPlace(BlockPlaceEvent event, ItemStack placedItem) {
        // Not cancelling - let the sponge do its thing, and then just give it back to the player (dried or otherwise)

        Player player = event.getPlayer();

        ItemStack cloned = placedItem.clone();
        cloned.setAmount(1);

        Block block = event.getBlock();

        // Wait until next tick
        WbsEnchants.getInstance().runSync(() -> {
            if (block.getType() != Material.SPONGE && block.getType() != Material.WET_SPONGE) {
                return;
            }

            EntityUtils.giveSafely(player, cloned);
            block.setType(Material.AIR);
        });
    }
}
