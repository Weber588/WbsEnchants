package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ConflictEnchantment;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;

public class CurseVanilla extends WbsEnchantment implements ConflictEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A curse that does nothing alone, but is incompatible with " +
            "all non-vanilla enchants!";

    public CurseVanilla() {
        super("curse/vanilla", DEFAULT_DESCRIPTION);

        // TODO: Replace using this with something like just... #enchantable? Is that possible, since custom
        //  enchants can go outside that?
        supportedItems = ItemTypeTagKeys.ENCHANTABLE_VANISHING;
        exclusiveWith = WbsEnchantsBootstrap.CUSTOM;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Vanilla";
    }

    @Override
    public String getConflictsDescription() {
        return "All custom enchantments";
    }

    @Override
    public EnchantmentType getType() {
        return EnchantmentTypeManager.CURSE;
    }
}
