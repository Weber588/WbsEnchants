package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.EnchantUtils;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.List;
import java.util.Map;

public class WickedEnchant extends WbsEnchantment implements DamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "This enchantment greatly increases attack damage for every " +
            "curse on the same item!";

    private static final WbsParticleGroup HIT_EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect()
                    .setXYZ(1)
                    .setSpeed(0.2)
                    .setAmount(25),
                    Particle.MYCELIUM
            );
    public static final String IGNORE_ENCHANTMENTS_KEY = "ignore-enchantments";

    private List<String> ignoreRegex = List.of(
            "stellarity:.*"
    );

    public WickedEnchant() {
        super("wicked", EnchantmentTypeManager.PARADOXICAL, DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_WEAPON);
    }

    @Override
    public void configure(@NotNull ConfigurationSection section, String directory) {
        super.configure(section, directory);

        ignoreRegex = section.getStringList(IGNORE_ENCHANTMENTS_KEY);
    }

    @Override
    public @NotNull ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection built = super.buildConfigurationSection(baseFile);

        built.set(IGNORE_ENCHANTMENTS_KEY, ignoreRegex);
        built.setComments(IGNORE_ENCHANTMENTS_KEY, List.of("Enchantment keys added here won't be considered curses.", "Supports regex!"));

        return built;
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        ItemStack item = EntityUtils.getEnchantedFromSlot(attacker, this, EquipmentSlot.HAND);

        if (item != null) {
            double multiplier = 1;
            Map<Enchantment, Integer> enchantments = item.getEnchantments();

            for (Enchantment enchant : enchantments.keySet()) {
                if (EnchantUtils.isCurse(enchant)) {
                    if (ignoreRegex.stream().anyMatch(regex -> enchant.getKey().asString().matches(regex))) {
                        continue;
                    }

                    double levelMultiplier = 0.5 + (0.5 * enchantments.get(enchant));

                    double rarityMultiplier = 1.0 / enchant.getWeight();

                    multiplier += levelMultiplier * rarityMultiplier;
                }
            }

            if (multiplier > 1) {
                event.setDamage(event.getDamage() * multiplier);
                HIT_EFFECT.play(WbsEntityUtil.getMiddleLocation(victim));
            }
        }
    }
}
