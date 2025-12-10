package wbs.enchants.enchantment;

import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.AbstractScaleEnchantment;

public class ShrinkingEnchant extends AbstractScaleEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Shrinks the wearer.";

    public ShrinkingEnchant() {
        super("shrinking", DEFAULT_DESCRIPTION);

        scale = -0.25f;

        getDefinition()
                .targetDescription("Saddles, Harnesses, and Armor Stands.")
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_SCALABLE_DOWN)
                .activeSlots(EquipmentSlotGroup.BODY, EquipmentSlotGroup.SADDLE);
    }
}
