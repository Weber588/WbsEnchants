package wbs.enchants.enchantment.shulkerbox;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ContainerItemEnchant;
import wbs.enchants.enchantment.helper.ContainerItemWrapper;

import java.util.List;
import java.util.function.Consumer;

public class RefillEnchant extends WbsEnchantment implements ContainerItemEnchant {
    private static final String DEFAULT_DESCRIPTION = "When you use the last item of a stack in your inventory, if an " +
            "item of the same type is in the enchanted item, it will be immediately placed into your hand.";

    public RefillEnchant() {
        super("refill", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_ITEM_CONTAINER);
    }


    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlaceBlock(BlockPlaceEvent event) {
        ItemStack placed = event.getItemInHand();

        if (placed.getAmount() == 1) {
            Player player = event.getPlayer();

            replaceExpendedItem(player, placed, stack -> player.getInventory().setItem(event.getHand(), stack));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemDurabilityDamage(PlayerItemBreakEvent event) {
        ItemStack broken = event.getBrokenItem();

        Player player = event.getPlayer();
        PlayerInventory playerInv = player.getInventory();

        replaceExpendedItem(player, broken, playerInv::setItemInMainHand);
    }

    @EventHandler
    public void onEatFood(PlayerItemConsumeEvent event) {
        ItemStack consumed = event.getItem();

        Player player = event.getPlayer();
        if (consumed.getAmount() == 1) {
            replaceExpendedItem(player, consumed, stack -> consumed.setAmount(stack.getAmount()));
        }
    }

    @EventHandler
    public void onShoot(PlayerLaunchProjectileEvent event) {
        ItemStack fired = event.getItemStack();

        Player player = event.getPlayer();
        PlayerInventory playerInv = player.getInventory();

        if (fired.getAmount() == 1) {
            replaceExpendedItem(player, fired, playerInv::addItem);
        }
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack fired = event.getConsumable();

        PlayerInventory playerInv = player.getInventory();

        if (fired != null && fired.getAmount() == 1) {    
            replaceExpendedItem(player, fired, playerInv::addItem);
        }
    }

    private void replaceExpendedItem(Player player, ItemStack used, Consumer<ItemStack> returnItem) {
        List<ContainerItemWrapper> refillItems = getContainerItemWrappers(player);

        if (refillItems.isEmpty()) {
            return;
        }

        for (ContainerItemWrapper wrapper : refillItems) {
            for (ItemStack stack : wrapper.getItems()) {
                if (stack != null && stack.isSimilar(used)) {
                    wrapper.removeItem(stack);

                    returnItem.accept(stack);

                    wrapper.saveToItem();

                    WbsEnchants.getInstance().buildMessage("Item replaced from ")
                            .append(wrapper.displayName())
                            .append("&r!")
                            .build()
                            .sendActionBar(player);
                    return;
                }
            }
        }
    }
}
