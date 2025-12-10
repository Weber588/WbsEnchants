package wbs.enchants.enchantment.helper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

import java.util.List;

public abstract class AbstractScaleEnchantment extends WbsEnchantment implements EntityEnchant {
    protected float scale = 0;

    public AbstractScaleEnchantment(@NotNull String key, @NotNull String description) {
        super(key, description);

        getDefinition()
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_SCALABLE)
                .maxLevel(2);
    }

    public void configure(@NotNull ConfigurationSection section, String directory) {
        super.configure(section, directory);

        scale = (float) (section.getDouble("scale-percent", scale * 100) / 100);

        EnchantmentAttributeEffect attributeEffect = new EnchantmentAttributeEffect(
                ResourceLocation.fromNamespaceAndPath(WbsEnchantsBootstrap.NAMESPACE, key().value()),
                Attributes.SCALE,
                LevelBasedValue.perLevel(scale, scale),
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        getDefinition()
                .effects()
                .set(EnchantmentEffectComponents.ATTRIBUTES, List.of(attributeEffect));
    }

    @Override
    public boolean canEnchant(Entity entity) {
        return entity instanceof Attributable;
    }

    @Override
    public void afterPlace(PlaceContext context) {
        Entity entity = context.entity();

        if (entity instanceof Attributable attributable) {
            AttributeInstance attribute = attributable.getAttribute(Attribute.SCALE);
            if (attribute != null) {
                org.bukkit.attribute.AttributeModifier modifier = new org.bukkit.attribute.AttributeModifier(
                        getKey(),
                        context.level() * scale,
                        org.bukkit.attribute.AttributeModifier.Operation.ADD_SCALAR
                );
                attribute.addModifier(modifier);
            }
        }
    }
}
