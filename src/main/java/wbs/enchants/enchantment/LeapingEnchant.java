package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
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

public class LeapingEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Increases jump height of mobs wearing the saddle.";

    public LeapingEnchant() {
        super("leaping", DEFAULT_DESCRIPTION);

        EnchantmentAttributeEffect attributeEffect = new EnchantmentAttributeEffect(
                ResourceLocation.fromNamespaceAndPath(WbsEnchantsBootstrap.NAMESPACE, key().value()),
                Attributes.JUMP_STRENGTH,
                LevelBasedValue.perLevel(0.05f, 0.05f),
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        getDefinition()
                .supportedItems(ItemTypeKeys.SADDLE)
                .activeSlots(EquipmentSlotGroup.SADDLE)
                .maxLevel(3)
                .effects()
                    .set(EnchantmentEffectComponents.ATTRIBUTES, List.of(attributeEffect));
    }
}
