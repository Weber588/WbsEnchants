package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.utils.util.entities.WbsEntityUtil;

public class SurfaceMinerEnchant extends WbsEnchantment implements TickableEnchant {
    private static final String DEFAULT_DESCRIPTION = "Grants Haste while you can see the sky.";

    public SurfaceMinerEnchant() {
        super("surface_miner", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeTagKeys.PICKAXES)
                .activeSlots(EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public int getTickFrequency() {
        return 20;
    }

    @Override
    public void onTickEquipped(LivingEntity owner) {
        if (!(owner instanceof Player player)) {
            return;
        }

        ItemStack highestEnchanted = getHighestEnchanted(owner);

        if (WbsEntityUtil.canSeeSky(owner)) {
            PotionEffect effect = new PotionEffect(PotionEffectType.HASTE,
                    (int) (getTickFrequency() * 1.5),
                    getLevel(highestEnchanted) - 1,
                    true,
                    false,
                    true);

            player.addPotionEffect(effect);
        }
    }
}
