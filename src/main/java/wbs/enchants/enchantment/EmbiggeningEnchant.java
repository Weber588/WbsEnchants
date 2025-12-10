package wbs.enchants.enchantment;

import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.AbstractScaleEnchantment;

public class EmbiggeningEnchant extends AbstractScaleEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Enlarges the wearer.";

    public EmbiggeningEnchant() {
        super("embiggening", DEFAULT_DESCRIPTION);

        scale = 0.25f;

        getDefinition()
                .targetDescription("Saddles and Armor Stands.")
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_SCALABLE_UP)
                .activeSlots(EquipmentSlotGroup.SADDLE);
    }
}
