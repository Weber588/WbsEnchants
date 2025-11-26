package wbs.enchants.enchantment.helper;

import wbs.enchants.WbsEnchantment;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.string.WbsStrings;

public class WbsCurse extends WbsEnchantment {
    public WbsCurse(String subKey, String description) {
        this(subKey,
                "Curse of " + WbsStrings.capitalizeAll(subKey.replaceAll("[_-]", " ")),
                description);
    }
    public WbsCurse(String subKey, String displayName, String description) {
        super("curse/" + subKey,
                EnchantmentTypeManager.CURSE,
                displayName,
                description);

        // Defaults for curses in vanilla
        getDefinition()
                .minimumCost(25, 0)
                .maximumCost(50, 0)
                .anvilCost(8);
    }
}
