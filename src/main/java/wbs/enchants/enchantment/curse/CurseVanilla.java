package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ConflictEnchantment;
import wbs.enchants.enchantment.helper.WbsCurse;

public class CurseVanilla extends WbsCurse implements ConflictEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A curse that does nothing alone, but is incompatible with " +
            "all non-vanilla enchants.";

    public CurseVanilla() {
        super("vanilla",  DEFAULT_DESCRIPTION);

        getDefinition()
                // TODO: Replace using this with something like just... #enchantable? Is that possible, since custom
                //  enchants can go outside that?
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_VANISHING)
                .exclusiveWith(WbsEnchantsBootstrap.CUSTOM);

    }

    @Override
    public String getConflictsDescription() {
        return "All custom enchantments";
    }
}
