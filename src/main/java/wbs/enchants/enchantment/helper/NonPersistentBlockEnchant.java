package wbs.enchants.enchantment.helper;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EventUtils;

public interface NonPersistentBlockEnchant extends EnchantInterface {
    default void registerNonPersistentBlockEvents() {
        EventUtils.register(BlockPlaceEvent.class, this::onPlace);
    }

    default void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return;
        }

        if (!canEnchant(block)) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();

        ItemStack placedItem = equipment.getItem(event.getHand());
        if (enchant.containsEnchantment(placedItem)) {
            onPlace(event, placedItem);
        }
    }

    void onPlace(BlockPlaceEvent event, ItemStack placedItem);
    boolean canEnchant(Block block);
}
