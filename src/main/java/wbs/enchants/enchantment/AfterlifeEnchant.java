package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.EntityUtils;

public class AfterlifeEnchant extends WbsEnchantment {
    public static final String DESCRIPTION = "When this item breaks, this enchantment is removed and" +
            " the item is restored to full health.";

    public AfterlifeEnchant() {
        super("afterlife", DESCRIPTION);

        maxLevel = 1;
        supportedItems = ItemTypeTagKeys.ENCHANTABLE_DURABILITY;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Afterlife";
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        ItemStack brokenItem = event.getBrokenItem();
        if (isEnchantmentOn(brokenItem)) {
            Player player = event.getPlayer();

            brokenItem.removeEnchantment(getEnchantment());
            brokenItem.editMeta(Damageable.class, meta -> meta.setDamage(0));
            EntityUtils.giveSafely(player, brokenItem);

            Component message = brokenItem.displayName()
                    .append(Component.text(" broke, but "))
                    .append(displayName())
                    .append(Component.text(" saved it!"));

            player.sendActionBar(message);
        }
    }
}
