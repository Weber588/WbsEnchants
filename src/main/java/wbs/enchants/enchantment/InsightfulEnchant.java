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
import wbs.enchants.events.enchanting.ChooseEnchantmentHintEvent;
import wbs.enchants.events.enchanting.EnchantingContext;
import wbs.enchants.util.EnchantUtils;

import java.util.Map;

public class InsightfulEnchant extends WbsEnchantment implements EnchantingEnchant, BlockStateEnchant<ChiseledBookshelf> {
    private static final String DEFAULT_DESCRIPTION = "Ensures any enchantments contained in the chiseled bookshelf " +
            "will be the enchantment hint whenever a nearby enchanting table would generate it.";

    public InsightfulEnchant() {
        super("insightful", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(ItemTypeKeys.CHISELED_BOOKSHELF)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_CHISELED_BOOKSHELF);
    }

    @Override
    public Class<ChiseledBookshelf> getStateClass() {
        return ChiseledBookshelf.class;
    }

    @EventHandler
    public void onGetAvailableEnchants(ChooseEnchantmentHintEvent event) {
        EnchantingContext context = event.getContext();

        for (Block powerProviderBlock : context.powerProviderBlocks()) {
            Integer level = getLevel(powerProviderBlock);

            if (level != null) {
                if (powerProviderBlock.getState() instanceof TileStateInventoryHolder inventoryBlock) {
                    for (ItemStack book : inventoryBlock.getInventory()) {
                        if (book != null) {
                            Map<Enchantment, Integer> storedEnchantments = EnchantUtils.getStoredEnchantments(book);

                            for (Enchantment stored : storedEnchantments.keySet()) {
                                if (event.getEnchantments().containsKey(stored)) {
                                    event.setChosenEnchantment(stored);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
