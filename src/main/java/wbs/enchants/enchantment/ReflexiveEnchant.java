package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.DamageEnchant;
import wbs.enchants.util.ItemUtils;

public class ReflexiveEnchant extends WbsEnchantment implements DamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "When taking damage from a mob while this is anywhere " +
            "but in your hand, you'll instantly switch to it, so you're ready for combat!";

    public ReflexiveEnchant() {
        super("reflexive", DEFAULT_DESCRIPTION);

        getDefinition()
                .activeSlots(EquipmentSlotGroup.HAND)
                .supportedItems(ItemTypeKeys.SHIELD);
    }

    @Override
    public void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (victim instanceof Player player) {
            PlayerInventory inventory = player.getInventory();

            if (ItemUtils.isBlockingItem(inventory.getItemInMainHand())) {
                return;
            }

            ItemStack offHandItem = inventory.getItemInOffHand();
            offHandItem.getType().getEquipmentSlot();
            if (ItemUtils.isBlockingItem(offHandItem)) {
                return;
            }

            ItemStack fromInventory = getHighestEnchantedAnywhere(player);
            if (fromInventory != null) {
                inventory.remove(fromInventory);
                inventory.setItemInOffHand(fromInventory);
                inventory.addItem(offHandItem);
            }
        }
    }
}
