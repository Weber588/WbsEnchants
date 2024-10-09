package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ConflictEnchantment;

import java.util.List;

public class CurseExotic extends WbsEnchantment implements ConflictEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A curse that does nothing alone, but is incompatible with " +
            "all vanilla enchants";

    public CurseExotic() {
        super("curse/exotic", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.ENCHANTABLE_VANISHING;
        exclusiveWith = WbsEnchantsBootstrap.VANILLA;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of the Exotic";
    }

    @Override
    public String getConflictsDescription() {
        return "All vanilla enchantments.";
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull List<TagKey<Enchantment>> addToTags() {
        return List.of(
                EnchantmentTagKeys.CURSE
        );
    }
}
