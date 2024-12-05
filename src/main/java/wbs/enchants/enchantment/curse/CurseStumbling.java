package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.EntityUtils;

import java.util.List;

public class CurseStumbling extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A curse that causes increased fall damage when worn on boots; " +
            "essentially the opposite of Feather Falling.";

    public CurseStumbling() {
        super("curse/stumbling", DEFAULT_DESCRIPTION);

        maxLevel = 4;
        supportedItems = ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR;
        exclusiveWith = WbsEnchantsBootstrap.EXCLUSIVE_SET_FALL_DAMAGE_AFFECTING;
        weight = 10;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Stumbling";
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        ItemStack boots = EntityUtils.getEnchantedFromSlot(entity, this, EquipmentSlot.FEET);

        if (boots != null) {
            double damage = event.getDamage();
            damage *= (1 + (0.12 * getLevel(boots)));
            event.setDamage(damage);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull List<TagKey<Enchantment>> addToTags() {
        return List.of(
                WbsEnchantsBootstrap.EXCLUSIVE_SET_FALL_DAMAGE_AFFECTING
        );
    }

    @Override
    public EnchantmentType getType() {
        return EnchantmentTypeManager.CURSE;
    }
}
