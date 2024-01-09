package wbs.enchants.util;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsMath;

import java.util.Objects;

public class ItemUtils {
    public static boolean damageItem(Player player, ItemStack stack, int damage) {
        return damageItem(player, stack, damage, null);
    }

    public static boolean damageItem(Player player, ItemStack stack, int damage, @Nullable EquipmentSlot slot) {
        if (!(stack.getItemMeta() instanceof Damageable damageable)) {
            return false;
        }

        if (damageable.isUnbreakable()) {
            return false;
        }

        int unbreakingLevel = stack.getEnchantmentLevel(Enchantment.DURABILITY);
        if (unbreakingLevel > 0) {
            if (!WbsMath.chance(100.0 / unbreakingLevel)) {
                return false;
            }
        }

        if (damage <= 0) {
            return false;
        }

        int currentDamage = damageable.getDamage();

        PlayerItemDamageEvent itemDamageEvent = new PlayerItemDamageEvent(player, stack, damage);

        Bukkit.getPluginManager().callEvent(itemDamageEvent);

        if (!itemDamageEvent.isCancelled()) {
            damageable.setDamage(currentDamage + damage);

            stack.setItemMeta(damageable);
            if (damageable.getDamage() > stack.getType().getMaxDurability()) {
                stack.setItemMeta(damageable);
                breakItem(player, stack, slot);
            }
            return true;
        }

        return false;
    }

    public static void breakItem(Player player, ItemStack stack, @Nullable EquipmentSlot slot) {
        if (slot == null) {
            stack.setAmount(0);
            player.playEffect(EntityEffect.BREAK_EQUIPMENT_MAIN_HAND);
            return;
        }

        EntityEquipment equipment = Objects.requireNonNull(player.getEquipment());
        EntityEffect effect = switch (slot) {
            case HAND -> EntityEffect.BREAK_EQUIPMENT_MAIN_HAND;
            case OFF_HAND -> EntityEffect.BREAK_EQUIPMENT_OFF_HAND;
            case FEET -> EntityEffect.BREAK_EQUIPMENT_BOOTS;
            case LEGS -> EntityEffect.BREAK_EQUIPMENT_LEGGINGS;
            case CHEST -> EntityEffect.BREAK_EQUIPMENT_CHESTPLATE;
            case HEAD -> EntityEffect.BREAK_EQUIPMENT_HELMET;
        };

        player.playEffect(effect);
        equipment.setItem(slot, null);
    }

    public static boolean isProperTool(Block block, ItemStack stack) {
        Material material = block.getType();
        Material itemType = stack.getType();

        if (Tag.MINEABLE_PICKAXE.isTagged(material)) {
            return Tag.ITEMS_PICKAXES.isTagged(itemType);
        }
        if (Tag.MINEABLE_SHOVEL.isTagged(material)) {
            return Tag.ITEMS_SHOVELS.isTagged(itemType);
        }
        if (Tag.MINEABLE_AXE.isTagged(material)) {
            return Tag.ITEMS_AXES.isTagged(itemType);
        }
        if (Tag.MINEABLE_HOE.isTagged(material)) {
            return Tag.ITEMS_HOES.isTagged(itemType);
        }

        return block.isPreferredTool(stack);
    }
}
