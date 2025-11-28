package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.block.EnchantingTable;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;
import wbs.enchants.enchantment.helper.EnchantingEnchant;
import wbs.enchants.events.enchanting.ChooseEnchantmentCostEvent;
import wbs.enchants.events.enchanting.EnchantingPreparationContext;
import wbs.enchants.util.EnchantingEventUtils;

public class PowerlustEnchant extends WbsEnchantment implements EnchantingEnchant, BlockStateEnchant<EnchantingTable> {
    @Override
    public Class<EnchantingTable> getStateClass() {
        return EnchantingTable.class;
    }
    private static final @NotNull String DEFAULT_DESCRIPTION = "Makes all 3 offered enchantments use the highest level that " +
            "the enchanting table can offer.";

    public PowerlustEnchant() {
        super("powerlust", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(ItemTypeKeys.ENCHANTING_TABLE)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_ENCHANTING_TABLE);
    }

    @EventHandler
    public void onChooseCost(ChooseEnchantmentCostEvent event) {
        EnchantingPreparationContext context = event.getContext();

        Integer level = getLevel(context.enchantingBlock());
        if (level == null) {
            return;
        }

        int cost = EnchantingEventUtils.getEnchantmentCost(
                event.getSeed() + event.getSlot(),
                2,
                event.getPower());

        event.setCost(cost);
    }
}
