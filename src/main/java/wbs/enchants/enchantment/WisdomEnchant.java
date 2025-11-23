package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

public class WisdomEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Removes XP pickup cooldown.";
    public WisdomEnchant() {
        super("wisdom", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR)
                .activeSlots(EquipmentSlotGroup.HEAD)
                .maxLevel(1)
                .weight(1)
                .anvilCost(1);
    }

    @EventHandler
    public void onPickupXP(PlayerExpCooldownChangeEvent event) {
        ItemStack item = getHighestEnchanted(event.getPlayer());
        if (item != null) {
            event.setNewCooldown(0);
        }
    }
}
