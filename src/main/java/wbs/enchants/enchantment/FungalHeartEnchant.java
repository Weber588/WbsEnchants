package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.TickableEnchant;

public class FungalHeartEnchant extends WbsEnchantment implements TickableEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Grants Resistance while standing on Mycelium.";

    public FungalHeartEnchant() {
        super("fungal_heart", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_CHEST_ARMOR)
                .activeSlots(EquipmentSlotGroup.CHEST)
                .maxLevel(3);
    }

    @Override
    public int getTickFrequency() {
        return 20;
    }


    @Override
    public void onTickEquipped(LivingEntity owner) {
        ItemStack highestEnchanted = getHighestEnchanted(owner);

        if (owner.getLocation().subtract(0, 0.1, 0).getBlock().getType() == Material.MYCELIUM) {
            PotionEffect effect = new PotionEffect(PotionEffectType.RESISTANCE,
                    (int) (getTickFrequency() * 1.5),
                    getLevel(highestEnchanted) - 1,
                    true,
                    false,
                    true);

            owner.addPotionEffect(effect);
        }
    }
}
