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
import wbs.enchants.util.EnchantUtils;

import java.util.Map;
import java.util.Random;

public class AbjurationEnchant extends WbsEnchantment implements EnchantingEnchant, BlockStateEnchant<ChiseledBookshelf> {
    private static final String DEFAULT_DESCRIPTION = "Prevents any enchantments contained in the chiseled bookshelf " +
            "from appearing on any enchanting table that uses it for power.";
    private static final double CHANCE_PER_LEVEL = 20;

    public AbjurationEnchant() {
        super("abjuration", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.CHISELED_BOOKSHELF)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_CHISELED_BOOKSHELF);
    }

    @Override
    public Class<ChiseledBookshelf> getStateClass() {
        return ChiseledBookshelf.class;
    }

    @EventHandler
    public void onGetAvailableEnchants(GetAvailableEnchantsEvent event) {
        EnchantingContext context = event.getContext();

        Random random = new Random(event.getContext().seed());
        for (Block powerProviderBlock : context.powerProviderBlocks()) {
            Integer level = getLevel(powerProviderBlock);

            if (level != null && (random.nextDouble() * 100 < CHANCE_PER_LEVEL * level)) {
                if (powerProviderBlock.getState() instanceof TileStateInventoryHolder inventoryBlock) {
                    for (ItemStack book : inventoryBlock.getInventory()) {
                        if (book != null) {
                            Map<Enchantment, Integer> storedEnchantments = EnchantUtils.getStoredEnchantments(book);

                            event.getAvailableOnTable().removeAll(storedEnchantments.keySet());
                        }
                    }
                }
            }
        }
    }
}
