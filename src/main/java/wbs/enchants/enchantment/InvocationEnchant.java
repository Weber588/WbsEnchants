package wbs.enchants.enchantment;

import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.block.Block;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;
import wbs.enchants.enchantment.helper.EnchantingEnchant;
import wbs.enchants.events.enchanting.EnchantingContext;
import wbs.enchants.events.enchanting.GetAvailableEnchantsEvent;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.EnchantUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class InvocationEnchant extends WbsEnchantment implements EnchantingEnchant, BlockStateEnchant<ChiseledBookshelf> {
    private static final String DEFAULT_DESCRIPTION = "Allows any enchantments contained in the chiseled bookshelf " +
            "to appear on any enchanting table that uses it for power.";
    private static final double CHANCE_PER_LEVEL = 20;

    public InvocationEnchant() {
        super("invocation", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.CHISELED_BOOKSHELF)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_CHISELED_BOOKSHELF);
    }

    @Override
    public Class<ChiseledBookshelf> getStateClass() {
        return ChiseledBookshelf.class;
    }

    /*
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnchantingTableAdd(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();

        Enchantable enchantableData = item.getData(DataComponentTypes.ENCHANTABLE);
        if (enchantableData == null) {
            boolean addEnchantability = false;
            blockLoop: for (Block powerProviderBlock : EnchantingEventUtils.getPowerProviderBlocks(event.getEnchantBlock())) {
                if (isEnchanted(powerProviderBlock)) {
                    Map<Enchantment, Integer> storedEnchantments = getStoredEnchants(powerProviderBlock);
                    for (Enchantment enchantment : storedEnchantments.keySet()) {
                        if (enchantment.canEnchantItem(item)) {
                            addEnchantability = true;
                            break blockLoop;
                        }
                    }
                }
            }

            if (addEnchantability) {
                enchantableData = Enchantable.enchantable(WbsEnchants.getInstance().getSettings().defaultEnchantability());

                item.setData(DataComponentTypes.ENCHANTABLE, enchantableData);
            }
        }
    }

     */

    @EventHandler
    public void onGetAvailableEnchants(GetAvailableEnchantsEvent event) {
        EnchantingContext context = event.getContext();

        Random random = new Random(event.getContext().seed());
        for (Block powerProviderBlock : context.powerProviderBlocks()) {
            Integer level = getLevel(powerProviderBlock);

            if (level != null && (random.nextDouble() * 100 < CHANCE_PER_LEVEL * level)) {
                Map<Enchantment, Integer> storedEnchantments = getStoredEnchants(powerProviderBlock);

                Set<Enchantment> toAdd = storedEnchantments.keySet();

                toAdd.removeIf(ench -> EnchantmentTypeManager.getType(ench).equals(EnchantmentTypeManager.ETHEREAL));

                event.getAvailableOnTable().addAll(toAdd);
            }
        }
    }

    private static Map<Enchantment, Integer> getStoredEnchants(Block powerProviderBlock) {
        Map<Enchantment, Integer> storedEnchantments = new HashMap<>();

        if (powerProviderBlock.getState() instanceof TileStateInventoryHolder inventoryBlock) {
            for (ItemStack book : inventoryBlock.getInventory()) {
                if (book != null) {
                    storedEnchantments.putAll(EnchantUtils.getStoredEnchantments(book));
                }
            }
        }

        return storedEnchantments;
    }
}
