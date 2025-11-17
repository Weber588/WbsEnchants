package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

import java.util.List;

public class StridingEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Increases step height.";

    public StridingEnchant() {
        super("striding", DEFAULT_DESCRIPTION);

        EnchantmentAttributeEffect stepHeight = new EnchantmentAttributeEffect(
                ResourceLocation.fromNamespaceAndPath(WbsEnchantsBootstrap.NAMESPACE, key().value()),
                Attributes.STEP_HEIGHT,
                LevelBasedValue.perLevel(1f, 1f),
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_LEG_ARMOR)
                .activeSlots(EquipmentSlotGroup.LEGS)
                .maxLevel(1)
                .effects()
                    .set(EnchantmentEffectComponents.ATTRIBUTES, List.of(stepHeight));
    }
}
