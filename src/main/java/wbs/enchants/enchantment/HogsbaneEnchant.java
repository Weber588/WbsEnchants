package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.TargetedDamageEnchant;

import java.util.Set;

public class HogsbaneEnchant extends TargetedDamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "A damage enchantment that does extra damage to pig-like mobs, " +
            "such as pigs, hoglins, piglins, and variants!";

    public HogsbaneEnchant() {
        super("hogsbane", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.ENCHANTABLE_WEAPON;
        maxLevel = 5;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Hogsbane";
    }

    @Override
    protected @NotNull Set<EntityType> getDefaultMobs() {
        return Set.of(
                EntityType.PIG,
                EntityType.PIGLIN,
                EntityType.ZOMBIFIED_PIGLIN,
                EntityType.PIGLIN_BRUTE,
                EntityType.HOGLIN,
                EntityType.ZOGLIN
        );
    }
}
