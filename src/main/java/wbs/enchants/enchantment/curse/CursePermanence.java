package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.WbsCurse;

public class CursePermanence extends WbsCurse {
    private static final String DEFAULT_DESCRIPTION = "Prevents this item from being used in an anvil or otherwise repaired.";

    public CursePermanence() {
        super("permanence", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_DURABILITY)
                .exclusiveWith(WbsEnchantsBootstrap.EXCLUSIVE_SET_SELF_REPAIRING);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilUse(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getFirstItem();
        if (firstItem != null && isEnchantmentOn(firstItem)) {
            event.setResult(ItemStack.empty());
        }
    }
}
