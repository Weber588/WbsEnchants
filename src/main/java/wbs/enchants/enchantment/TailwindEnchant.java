package wbs.enchants.enchantment;

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

public class TailwindEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Increases the flight speed of happy ghasts.";

    public TailwindEnchant() {
        super("tailwind", DEFAULT_DESCRIPTION);

        EnchantmentAttributeEffect attributeEffect = new EnchantmentAttributeEffect(
                ResourceLocation.fromNamespaceAndPath(WbsEnchantsBootstrap.NAMESPACE, key().value()),
                Attributes.FLYING_SPEED,
                LevelBasedValue.perLevel(0.5f, 0.5f),
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_HARNESS)
                .activeSlots(EquipmentSlotGroup.BODY)
                .maxLevel(3)
                .effects()
                .set(EnchantmentEffectComponents.ATTRIBUTES, List.of(attributeEffect));
    }
}
