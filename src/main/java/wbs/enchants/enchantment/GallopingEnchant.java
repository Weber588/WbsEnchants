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

public class GallopingEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Speeds up mobs wearing the saddle.";

    public GallopingEnchant() {
        super("galloping", DEFAULT_DESCRIPTION);

        EnchantmentAttributeEffect movementSpeed = new EnchantmentAttributeEffect(
                ResourceLocation.fromNamespaceAndPath(WbsEnchantsBootstrap.NAMESPACE, key().value()),
                Attributes.MOVEMENT_SPEED,
                LevelBasedValue.perLevel(0.1f, 0.1f),
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        getDefinition()
                .supportedItems(ItemTypeKeys.SADDLE)
                .activeSlots(EquipmentSlotGroup.SADDLE)
                .maxLevel(3)
                .effects()
                    .set(EnchantmentEffectComponents.ATTRIBUTES, List.of(movementSpeed));
    }
}
