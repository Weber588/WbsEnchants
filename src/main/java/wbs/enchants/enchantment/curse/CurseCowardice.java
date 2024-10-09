package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

import java.util.List;

public class CurseCowardice extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A curse that turns your weapon fearful, reducing its damage " +
            "the closer your enemies get.";

    public CurseCowardice() {
        super("curse/cowardice", DEFAULT_DESCRIPTION);

        maxLevel = 1;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Cowardice";
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        ItemStack enchantedItem = getIfEnchanted(attacker);
        if (enchantedItem != null) {

            AttributeInstance reachAttribute = attacker.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE);
            if (reachAttribute != null) {
                double reach = reachAttribute.getValue();

                double distanceToEntity = event.getEntity().getLocation().distance(attacker.getLocation());
                double modifier = distanceToEntity / reach;

                event.setDamage(event.getDamage() * modifier);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull List<TagKey<Enchantment>> addToTags() {
        return List.of(
                EnchantmentTagKeys.CURSE
        );
    }
}
