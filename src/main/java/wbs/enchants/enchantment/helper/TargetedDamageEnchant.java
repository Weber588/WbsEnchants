package wbs.enchants.enchantment.helper;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.WbsEnums;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class TargetedDamageEnchant extends WbsEnchantment implements DamageEnchant {
    private static final String LIST_KEY = "affected-mobs";

    private final Set<EntityType> affectedMobTypes = new HashSet<>();

    public TargetedDamageEnchant(String key) {
        super(key);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        ItemStack item = EntityUtils.getEnchantedFromSlot(attacker, this, EquipmentSlot.HAND);

        if (item != null) {
            if (shouldAffect(victim)) {
                double damage = event.getDamage();

                double bonusDamage = getBonusDamage(victim) * getLevel(item);

                if (EntityUtils.willCrit(attacker)) {
                    bonusDamage *= 1.5;
                }

                damage += bonusDamage;

                event.setDamage(damage);

                onHit(event, attacker, victim, projectile);
            }
        }
    }

    protected boolean shouldAffect(Entity victim) {
        return affectedMobTypes.contains(victim.getType());
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public Set<Enchantment> getIndirectConflicts() {
        return Set.of(DAMAGE_ALL);
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        super.configure(section, directory);

        List<String> hogLikeMobs = section.getStringList(LIST_KEY);

        String mobsDir = directory + "/" + LIST_KEY;
        for (String typeString : hogLikeMobs) {
            EntityType type = WbsEnums.getEnumFromString(EntityType.class, typeString);

            if (type == null) {
                WbsEnchants.getInstance().settings.logError("Invalid entity type: \"" + typeString + "\".", mobsDir);
                continue;
            }

            this.affectedMobTypes.add(type);
        }

        if (this.affectedMobTypes.isEmpty()) {
            affectedMobTypes.addAll(getDefaultMobs());
        }
    }

    @Override
    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection section = super.buildConfigurationSection(baseFile);

        if (this.affectedMobTypes.isEmpty()) {
            affectedMobTypes.addAll(getDefaultMobs());
        }

        section.set(LIST_KEY, affectedMobTypes.stream().map(EntityType::name).collect(Collectors.toList()));

        return section;
    }

    @NotNull
    protected abstract Set<EntityType> getDefaultMobs();
    protected void onHit(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {

    }
    protected double getBonusDamage(Entity victim) {
        return 2;
    }
}
