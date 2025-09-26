package wbs.enchants.enchantment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.TickableEnchant;

import java.util.List;

public class CavalrousEnchant extends WbsEnchantment implements TickableEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Greatly increases the health of horses, and applies regeneration.";
    public static final PotionEffect REGEN_EFFECT = new PotionEffect(PotionEffectType.REGENERATION, 50, 0, true, false);

    public CavalrousEnchant() {
        super("cavalrous", DEFAULT_DESCRIPTION);

        EnchantmentAttributeEffect attributeEffect = new EnchantmentAttributeEffect(
                ResourceLocation.fromNamespaceAndPath(WbsEnchantsBootstrap.NAMESPACE, key().value()),
                Attributes.MAX_HEALTH,
                LevelBasedValue.perLevel(0.2f, 0.2f),
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_HORSE_ARMOR)
                .activeSlots(EquipmentSlotGroup.BODY)
                .maxLevel(3)
                .effects()
                    .set(EnchantmentEffectComponents.ATTRIBUTES, List.of(attributeEffect));
    }

    @Override
    public int getTickFrequency() {
        return 40;
    }

    @Override
    public void onTickEquipped(LivingEntity owner) {
        owner.addPotionEffect(REGEN_EFFECT);
    }
}
