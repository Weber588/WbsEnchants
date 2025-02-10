package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import wbs.enchants.enchantment.helper.WbsCurse;

public class CurseRot extends WbsCurse {
    private static final double MODIFIER_PER_LEVEL = 1.2;
    private static final String DEFAULT_DESCRIPTION = "When worn, regeneration is " + MODIFIER_PER_LEVEL + "x slower per level.";

    public CurseRot() {
        super("rot", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .activeSlots(EquipmentSlotGroup.ARMOR)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_ARMOR);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRegenerate(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        int totalLevels = this.getSumLevels(entity);

        if (totalLevels > 0) {
            event.setAmount(event.getAmount() / (Math.pow(MODIFIER_PER_LEVEL, totalLevels)));
        }
    }
}
