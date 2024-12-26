package wbs.enchants.enchantment;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.TargetedDamageEnchant;

import java.util.Set;

public class EnderspiteEnchant extends TargetedDamageEnchant {
    private static final String DESCRIPTION = "A targeted damage enchantment that affects ender mobs more strongly!";

    public EnderspiteEnchant() {
        super("enderspite", DESCRIPTION);

        maxLevel = 5;
    }

    @Override
    protected @NotNull Set<EntityType> getDefaultMobs() {
        return Set.of(
                EntityType.ENDERMITE,
                EntityType.ENDER_DRAGON,
                EntityType.ENDERMAN
        );
    }

    @Override
    public String getDefaultDisplayName() {
        return "Enderspite";
    }
}
