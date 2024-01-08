package wbs.enchants.events;

import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.WbsMath;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class LootGenerateEvents implements Listener {
    private static final Random RANDOM = new Random();

    @EventHandler
    public void onLootGenerate(LootGenerateEvent event) {
        List<WbsEnchantment> registered = EnchantsSettings.getRegistered();

        List<ItemStack> loot = event.getLoot();

        for (WbsEnchantment enchantment : registered) {
            Double chance = enchantment.getAddToChance(event.getLootTable());

            if (chance == null) {
                continue;
            }

            if (!WbsMath.chance(chance)) {
                continue;
            }

            for (ItemStack stack : loot) {
                if (tryAdd(enchantment, stack)) {
                    break;
                }
            }
        }
    }

    private boolean tryAdd(WbsEnchantment enchantment, ItemStack stack) {
        if (stack.getType() != Material.ENCHANTED_BOOK && !enchantment.canEnchantItem(stack)) {
            return false;
        }

        Set<Enchantment> existing = new HashSet<>();
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            existing = meta.getStoredEnchants().keySet();
        } else {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                existing = meta.getEnchants().keySet();
            }
        }

        for (Enchantment other : existing) {
            if (enchantment.conflictsWith(other)) {
                return false;
            }
        }

        int level;
        int maxLevel = enchantment.getMaxLevel();
        if (maxLevel < 1) {
            level = 0;
        } else {
            level = RANDOM.nextInt(maxLevel) + 1;
        }

        if (stack.getType() == Material.ENCHANTED_BOOK) {
            UberUtils.addStoredEnchantment(enchantment, stack, level);
        } else {
            UberUtils.addEnchantment(enchantment, stack, level);
        }

        return true;
    }
}
