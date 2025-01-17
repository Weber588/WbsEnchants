package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.TickableEnchant;

public class ManathirstEnchant extends WbsEnchantment implements TickableEnchant {
    private static final String DEFAULT_DESCRIPTION = "An alternative to mending, items with this enchantment will " +
            "slowly drain your XP bar to repair itself. Items are repaired slower than mending, " +
            "but will continuously self-repair so long as you have XP, even if you haven't gained any recently!";

    private int xpPerDura = 3;

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
    public void onTickItemStack(LivingEntity owner, ItemStack item, int slot) {
        if (!(owner instanceof Player player)) {
            return;
        }

        if (!player.isOnline() || player.getTotalExperience() < xpPerDura) {
            return;
        }

        if (!(item.getItemMeta() instanceof Damageable damageable) || !damageable.hasDamageValue()) {
            return;
        }

        if (damageable.getDamage() == 0) {
            return;
        }

        damageable.setDamage(damageable.getDamage() - 1);
        player.setTotalExperience(Math.max(0, player.getTotalExperience() - EnchantManager.MANATHIRST.xpPerDura));

        item.setItemMeta(damageable);
    }
}
