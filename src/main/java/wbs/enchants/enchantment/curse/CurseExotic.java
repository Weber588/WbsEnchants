package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ConflictEnchantment;
import wbs.enchants.type.EnchantmentTypeManager;

public class CurseExotic extends WbsEnchantment implements ConflictEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A curse that does nothing alone, but is incompatible with " +
            "all vanilla enchants";

    public CurseExotic() {
        super("curse/exotic", EnchantmentTypeManager.CURSE, "Curse of the Exotic", DEFAULT_DESCRIPTION);

        getDefinition()
                .exclusiveWith(WbsEnchantsBootstrap.VANILLA)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_VANISHING);
    }

    @Override
    public String getConflictsDescription() {
        return "All vanilla enchantments.";
    }
}
