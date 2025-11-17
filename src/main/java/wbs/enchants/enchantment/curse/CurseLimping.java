package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.WbsCurse;

import java.util.List;

public class CurseLimping extends WbsCurse {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Decreases step height.";

    public CurseLimping() {
        super("limping", DEFAULT_DESCRIPTION);

        EnchantmentAttributeEffect stepHeight = new EnchantmentAttributeEffect(
                ResourceLocation.fromNamespaceAndPath(WbsEnchantsBootstrap.NAMESPACE, key().value()),
                Attributes.STEP_HEIGHT,
                LevelBasedValue.perLevel(-0.5f, -0.5f),
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
