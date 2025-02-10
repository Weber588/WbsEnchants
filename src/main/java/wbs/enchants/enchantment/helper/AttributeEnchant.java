package wbs.enchants.enchantment.helper;

import com.google.common.collect.Multimap;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface AttributeEnchant extends ItemModificationEnchant {
    @Override
    default void unmodifyItem(ItemStack item) {
        Multimap<Attribute, AttributeModifier> toRemove = getAttributes();

        toRemove.forEach(item.getItemMeta()::removeAttributeModifier);
    }

    @Override
    default void modifyItem(ItemStack item) {
        Multimap<Attribute, AttributeModifier> toAdd = getAttributes();

        toAdd.forEach(item.getItemMeta()::addAttributeModifier);
    }

    @NotNull
    Multimap<Attribute, AttributeModifier> getAttributes();
}
