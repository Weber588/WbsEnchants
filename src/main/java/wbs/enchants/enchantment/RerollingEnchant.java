package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.BlockStateEnchant;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class RerollingEnchant extends WbsEnchantment implements BlockStateEnchant<EnchantingTable> {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Makes the enchanting table re-roll enchantments when you close the inventory.";

    public RerollingEnchant() {
        super("rerolling", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.ENCHANTING_TABLE)
                .exclusiveWith(EnchantManager.HANDHELD);
    }

    @Override
    public Class<EnchantingTable> getStateClass() {
        return EnchantingTable.class;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onPrepEnchant(PrepareItemEnchantEvent event) {
        if (!isEnchanted(event.getEnchantBlock())) {
            return;
        }

        event.getView().setEnchantmentSeed(new Random(event.getEnchanter().getEnchantmentSeed()).nextInt());
    }

    @EventHandler
    public void onClickEnchantSlot(InventoryClickEvent event) {
        if (event.getSlot() != 0) {
            return;
        }

        if (event.getClickedInventory() == null || !(event.getClickedInventory() instanceof EnchantingInventory enchantingInventory)) {
            return;
        }

        Location location = enchantingInventory.getLocation();
        if (location == null || !isEnchanted(location.getBlock())) {
            return;
        }

        event.getWhoClicked().setEnchantmentSeed(new Random(event.getWhoClicked().getEnchantmentSeed()).nextInt());
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Block block = event.getEnchantBlock();

        if (!isEnchanted(block)) {
            return;
        }

        Enchantment enchantmentHint = event.getEnchantmentHint();
        int levelHint = event.getLevelHint();

        event.getItem().addEnchantment(enchantmentHint, levelHint);
        Set<Enchantment> remove = new HashSet<>();
        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        for (Enchantment enchantment : enchantsToAdd.keySet()) {
            if (enchantment.conflictsWith(enchantmentHint)) {
                remove.add(enchantment);
            }
        }

        for (Enchantment enchantment : remove) {
            enchantsToAdd.remove(enchantment);
        }
    }
}
