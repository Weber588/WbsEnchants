package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.TargetedDamageEnchant;

import java.util.Set;

public class HogsbaneEnchant extends TargetedDamageEnchant {
    public HogsbaneEnchant() {
        super("hogsbane");
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

    @Override
    public @NotNull String getDescription() {
        return "A damage enchantment that does extra damage to pig-like mobs, such as pigs, hoglins, piglins, and variants!";
    }

    @Override
    public String getDisplayName() {
        return "&7Hogsbane";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.UNCOMMON;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }
}
