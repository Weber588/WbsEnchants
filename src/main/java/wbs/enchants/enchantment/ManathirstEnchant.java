package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.TickableEnchant;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class ManathirstEnchant extends WbsEnchantment implements TickableEnchant {
    private static final String DEFAULT_DESCRIPTION = "An alternative to mending, items with this enchantment will " +
            "slowly drain your XP bar to repair itself. Items are repaired slower than mending, " +
            "but will continuously self-repair so long as you have XP, even if you haven't gained any recently!";

    private int xpPerDura = 2;

    public ManathirstEnchant() {
        super("manathirst", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_DURABILITY)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_SELF_REPAIRING);
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        super.configure(section, directory);

        xpPerDura = section.getInt("xp-per-durability", xpPerDura);
    }

    @Override
    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection section = super.buildConfigurationSection(baseFile);
        section.set("xp-per-durability", xpPerDura);
        return section;
    }

    @Override
    public int getTickFrequency() {
        return 20;
    }

    @Override
    public void onTickItemStack(LivingEntity owner, Map<ItemStack, Integer> enchantedStacks) {
        if (!(owner instanceof Player player)) {
            return;
        }

        if (!player.isOnline()) {
            return;
        }

        for (ItemStack item : enchantedStacks.keySet()) {
            if (player.calculateTotalExperiencePoints() < xpPerDura) {
                continue;
            }

            Integer maxDamage = item.getData(DataComponentTypes.MAX_DAMAGE);
            if (maxDamage == null) {
                continue;
            }

            Integer damage = item.getData(DataComponentTypes.DAMAGE);

            if (damage == null || damage <= 0) {
                continue;
            }

            item.setData(DataComponentTypes.DAMAGE, damage - 1);

            player.setExperienceLevelAndProgress(Math.max(0, player.calculateTotalExperiencePoints() - xpPerDura));
        }
    }

}
