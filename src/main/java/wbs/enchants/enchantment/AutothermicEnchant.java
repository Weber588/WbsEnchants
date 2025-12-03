package wbs.enchants.enchantment;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;

public class AutothermicEnchant extends WbsEnchantment implements BlockStateEnchant<BlockState> {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Reduces the rate of fuel consumption";

    public AutothermicEnchant() {
        super("autothermic", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .weight(1)
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_FUELLED_BLOCK)
                .addInjectInto(WbsEnchantsBootstrap.HEAT_BASED_ENCHANTS)
                .exclusiveWith(WbsEnchantsBootstrap.COLD_BASED_ENCHANTS);
    }

    @Override
    public Class<BlockState> getStateClass() {
        return BlockState.class;
    }

    @EventHandler
    public void onBurnEvent(FurnaceBurnEvent event) {
        Integer level = getLevel(event.getBlock());
        if (level != null) {
            event.setBurnTime(event.getBurnTime() * (level + 1));
        }
    }

    @EventHandler
    public void onBurnEvent(BrewingStandFuelEvent event) {
        Integer level = getLevel(event.getBlock());
        if (level != null) {
            event.setFuelPower(event.getFuelPower() * (level + 1));
        }
    }
}
