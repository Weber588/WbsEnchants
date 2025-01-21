package wbs.enchants.enchantment.shulkerbox;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.ShulkerBoxEnchantment;

import java.util.List;
import java.util.function.Consumer;

public class RefillEnchant extends ShulkerBoxEnchantment {
    private static final String DEFAULT_DESCRIPTION = "When you use the last item of a stack in your inventory, if an " +
            "item of the same type is in the shulker box, it will be immediately placed into your hand.";

    public RefillEnchant() {
        super("refill", DEFAULT_DESCRIPTION);
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
        replaceExpendedItem(player, consumed, stack -> consumed.setAmount(stack.getAmount()));
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack dropped = event.getItemDrop().getItemStack();

        Player player = event.getPlayer();
        PlayerInventory playerInv = player.getInventory();

        replaceExpendedItem(player, dropped, playerInv::addItem);
    }

    @EventHandler
    public void onShoot(PlayerLaunchProjectileEvent event) {
        ItemStack fired = event.getItemStack();

        Player player = event.getPlayer();
        PlayerInventory playerInv = player.getInventory();

        replaceExpendedItem(player, fired, playerInv::addItem);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack fired = event.getConsumable();

        PlayerInventory playerInv = player.getInventory();

        replaceExpendedItem(player, fired, playerInv::addItem);
    }

    private void replaceExpendedItem(Player player, ItemStack used, Consumer<ItemStack> returnItem) {
        List<ShulkerBoxWrapper> refillBoxes = getEnchantedInInventory(player);

        if (refillBoxes.isEmpty()) {
            return;
        }

        for (ShulkerBoxWrapper box : refillBoxes) {
            Inventory checkForRefill = box.box().getInventory();
            for (ItemStack stack : checkForRefill) {
                if (stack != null && stack.isSimilar(used)) {
                    checkForRefill.removeItem(stack);

                    returnItem.accept(stack);

                    box.save();

                    WbsEnchants.getInstance().buildMessage("Item replaced from ")
                            .append(box.displayName())
                            .append("&r!")
                            .build()
                            .sendActionBar(player);
                    return;
                }
            }
        }
    }
}
