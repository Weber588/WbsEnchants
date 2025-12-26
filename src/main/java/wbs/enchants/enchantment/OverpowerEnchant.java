package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.enchants.events.enchanting.EnchantingDerivePowerEvent;
import wbs.enchants.events.enchanting.EnchantingPreparationContext;

public class OverpowerEnchant extends WbsEnchantment implements BlockEnchant {
    private static final String DEFAULT_DESCRIPTION = "Grants an additional enchantment power to any enchanting" +
            "table that uses it.";

    public OverpowerEnchant() {
        super("overpower", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(
                        ItemTypeKeys.BOOKSHELF,
                        ItemTypeKeys.CHISELED_BOOKSHELF);
    }

    @EventHandler
    public void onChoosePower(EnchantingDerivePowerEvent event) {

        EnchantingPreparationContext context = event.getContext();

        for (Block block : context.powerProviderBlocks()) {
            Integer level = getLevel(block);

            if (level != null) {
                event.setPower(event.getPower() + 1);
            }
        }
    }

    @Override
    public boolean canEnchant(Block block)  {
        return block.getType() == Material.BOOKSHELF;
    }
}
