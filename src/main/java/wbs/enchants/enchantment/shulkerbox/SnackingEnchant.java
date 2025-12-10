package wbs.enchants.enchantment.shulkerbox;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.Consumable;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ShulkerBoxEnchantment;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.entities.WbsPlayerUtil;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;

// TODO: Could this be adapted to also be a bundle enchant?
@SuppressWarnings("UnstableApiUsage")
public class SnackingEnchant extends WbsEnchantment implements ShulkerBoxEnchantment, TickableEnchant {
    private static final int MAX_COOLDOWN_TICKS = 20 * 120;

    private static final String DEFAULT_DESCRIPTION = "Automatically feeds you with items from the enchanted " +
            "shulker box, with a cooldown between each. Cooldown decreases with level.";
    private static final NamespacedKey LAST_SNACKED_KEY = WbsEnchantsBootstrap.createKey("last_snacked");

    public SnackingEnchant() {
        super("snacking", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_SHULKER_BOX)
                .maxLevel(3);
    }


    @Override
    public int getTickFrequency() {
        return 20 * 10;
    }

    @Override
    public void onTickItemStack(LivingEntity owner, ItemStack itemStack, int slot) {
        if (!(owner instanceof Player player)) {
            return;
        }

        PersistentDataContainer container = player.getPersistentDataContainer();
        Integer lastSnackedTick = container.get(LAST_SNACKED_KEY, PersistentDataType.INTEGER);

        int level = getLevel(itemStack);
        if (lastSnackedTick != null && lastSnackedTick + (MAX_COOLDOWN_TICKS / level) > Bukkit.getCurrentTick()) {
            return;
        }

        ShulkerBoxWrapper wrapper = getShulkerBox(itemStack);

        if (wrapper == null) {
            return;
        }

        Inventory inventory = wrapper.getInventory();

        // Using a collection of entries rather than a map because we need to index by ItemStack, but
        // doing so might lead to conflicts with hashCode if an identical item is in multiple slots
        List<AbstractMap.SimpleEntry<ItemStack, Integer>> edibleItemsInSlots = new LinkedList<>();
        int shulkerSlot = 0;
        for (ItemStack stack : inventory) {
            if (stack != null && stack.hasData(DataComponentTypes.FOOD)) {
                edibleItemsInSlots.add(new AbstractMap.SimpleEntry<>(stack, shulkerSlot));
            }
            shulkerSlot++;
        }
        if (edibleItemsInSlots.isEmpty()){
            return;
        }

        AbstractMap.SimpleEntry<ItemStack, Integer> toEatEntry = WbsCollectionUtil.getRandom(edibleItemsInSlots);

        ItemStack toEat = toEatEntry.getKey();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.world.item.ItemStack serverItem = ((CraftItemStack) toEat).handle;
        Consumable consumable = serverItem.getComponents().get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            consumable.onConsume(serverPlayer.level(), serverPlayer, serverItem);
        }

        WbsPlayerUtil.PlayerConsumeItemResult consumeResult = WbsPlayerUtil.consume(player, toEat);

        if (consumeResult.success()) {
            container.set(LAST_SNACKED_KEY, PersistentDataType.INTEGER, Bukkit.getCurrentTick());

            if (consumeResult.remainingItem() != null) {
                // Only remove 1 eaten
                ItemStack toEatSingle = new ItemStack(toEat);
                toEatSingle.setAmount(1);
                inventory.removeItem(toEatSingle);

                // If the slot is now free, replace that same slot with the remaining item.
                ItemStack leftOverStack = inventory.getItem(toEatEntry.getValue());
                if (leftOverStack == null || leftOverStack.isEmpty()) {
                    inventory.setItem(toEatEntry.getValue(), consumeResult.remainingItem());
                } else { // Otherwise, just add to inventory
                    inventory.addItem(consumeResult.remainingItem());
                }
            }
        }
    }
}
