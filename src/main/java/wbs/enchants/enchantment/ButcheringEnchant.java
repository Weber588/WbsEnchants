package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.TargetedDamageEnchant;
import wbs.enchants.util.EntityUtils;
import wbs.utils.util.WbsMath;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ButcheringEnchant extends TargetedDamageEnchant {
    private static final String FOOD_INCREASE_KEY = "food-percent-increase-per-level";

    private int foodPercentIncreasePerLevel = 15;

    public ButcheringEnchant() {
        super("butchering");
    }

    @EventHandler
    protected void onAnimalKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Animals)) {
            return;
        }

        Player player = entity.getKiller();

        ItemStack item = EntityUtils.getEnchantedFromSlot(player, this);

        if (item != null) {
            List<ItemStack> drops = event.getDrops();
            List<ItemStack> extras = new LinkedList<>();
            double increaseChance = foodPercentIncreasePerLevel * getLevel(item);

            for (ItemStack drop : drops) {
                if (drop.getType().isEdible()) {
                    int extraAmount = 0;
                    for (int i = 0; i < drop.getAmount(); i++) {    
                        if (WbsMath.chance(increaseChance)) {
                            extraAmount++;
                        }
                    }

                    if (extraAmount > 0) {
                        ItemStack extra = drop.clone();
                        extra.setAmount(extraAmount);
                        extras.add(extra);
                    }
                }
            }

            drops.addAll(extras);
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "A damage enchant that deals extra damage to animals, and causes extra food to drop!";
    }

    @Override
    protected @NotNull Set<EntityType> getDefaultMobs() {
        return Arrays.stream(EntityType.values())
                .filter(type -> {
                    Class<? extends Entity> entityClass = type.getEntityClass();
                    return entityClass != null && Animals.class.isAssignableFrom(entityClass);
                }).collect(Collectors.toSet());
    }

    @Override
    public String getDisplayName() {
        return "&7Butchering";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.COMMON;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        super.configure(section, directory);

        foodPercentIncreasePerLevel = section.getInt(FOOD_INCREASE_KEY, foodPercentIncreasePerLevel);
    }

    @Override
    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection section = super.buildConfigurationSection(baseFile);

        section.set(FOOD_INCREASE_KEY, foodPercentIncreasePerLevel);

        return section;
    }
}
