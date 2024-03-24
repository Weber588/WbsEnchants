package wbs.enchants.events;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.AnvilSettings;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.WbsEnums;

import java.util.Map;

public class AnvilEvents implements Listener {

    private record AnvilResult(@Nullable ItemStack result, int cost, int repairItemCountCost) {}

    public AnvilResult createResult(Player player, final ItemStack base, final ItemStack add, @Nullable String name) {
        int cost = 1;

        int i = 0;
        byte b0 = 0;
        byte b1 = 0;

        if (base.isEmpty()) {
            return new AnvilResult(null, 0, 0);
        }

        ItemStack baseClone = base.clone();
        ItemStack addClone = add.clone();

        Map<Enchantment, Integer> baseEnchants = baseClone.getEnchantments();

        int j = b0 + getRepairCost(baseClone) + (addClone.isEmpty() ? 0 : getRepairCost(addClone));

        int repairItemCountCost = 0;
        if (!addClone.isEmpty()) {
            boolean hasStoredEnchants = addClone.getType() == Material.ENCHANTED_BOOK && !EnchantUtils.getEnchantments(addClone).isEmpty();
            int k;
            int l;
            int i1;

            if (baseClone instanceof Damageable damageable && AnvilSettings.isRepairItemFor(baseClone, addClone)) {
                k = Math.min(damageable.getDamage(), baseClone.getType().getMaxDurability() / 4);
                if (k <= 0) {
                    return new AnvilResult(null, 0, 0);
                }

                for (i1 = 0; k > 0 && i1 < addClone.getAmount(); ++i1) {
                    l = damageable.getDamage() - k;
                    damageable.setDamage(l);
                    ++i;
                    k = Math.min(damageable.getDamage(), baseClone.getType().getMaxDurability() / 4);
                }

                repairItemCountCost = i1;
            } else {
                if (!hasStoredEnchants && (baseClone.getType() != addClone.getType() || !(baseClone instanceof Damageable))) {
                    return new AnvilResult(null, 0, 0);
                }

                if (baseClone instanceof Damageable damageable1 && !hasStoredEnchants) {
                    k = baseClone.getType().getMaxDurability() - damageable1.getDamage();
                    i1 = addClone.getType().getMaxDurability() - getDamageSafe(addClone);
                    l = i1 + baseClone.getType().getMaxDurability() * 12 / 100;
                    int j1 = k + l;
                    int k1 = baseClone.getType().getMaxDurability() - j1;

                    if (k1 < 0) {
                        k1 = 0;
                    }

                    if (k1 < damageable1.getDamage()) {
                        damageable1.setDamage(k1);
                        i += 2;
                    }
                }

                Map<Enchantment, Integer> stack2Enchants = EnchantUtils.getEnchantments(addClone);
                boolean anyValidEnchants = false;
                boolean hasInvalidEnchants = false;

                for (Enchantment enchantment : stack2Enchants.keySet()) {
                    if (enchantment != null) {
                        int l1 = baseEnchants.getOrDefault(enchantment, 0);
                        int i2 = stack2Enchants.get(enchantment);

                        i2 = l1 == i2 ? i2 + 1 : Math.max(i2, l1);
                        boolean canBeAdded = enchantment.canEnchantItem(baseClone);

                        if (player.getGameMode() == GameMode.CREATIVE || baseClone.getType() == Material.ENCHANTED_BOOK) {
                            canBeAdded = true;
                        }

                        for (Enchantment enchantment1 : baseEnchants.keySet()) {
                            if (enchantment1 != enchantment && enchantment.conflictsWith(enchantment1)) {
                                canBeAdded = false;
                                ++i;
                            }
                        }

                        if (!canBeAdded) {
                            hasInvalidEnchants = true;
                        } else {
                            anyValidEnchants = true;
                            if (i2 > enchantment.getMaxLevel()) {
                                i2 = enchantment.getMaxLevel();
                            }

                            baseEnchants.put(enchantment, i2);
                            int j2 = switch (enchantment.getRarity()) {
                                case COMMON -> 1;
                                case UNCOMMON -> 2;
                                case RARE -> 4;
                                case VERY_RARE -> 8;
                            };

                            if (hasStoredEnchants) {
                                j2 = Math.max(1, j2 / 2);
                            }

                            i += j2 * i2;
                            if (baseClone.getAmount() > 1) {
                                i = 40;
                            }
                        }
                    }
                }

                if (hasInvalidEnchants && !anyValidEnchants) {
                    return new AnvilResult(null, 0, 0);
                }
            }
        }

        if (name != null && !name.isBlank()) {
            if (!name.equals(baseClone.getHoverName().getString())) {
                b1 = 1;
                i += b1;
                baseClone.setHoverName(IChatBaseComponent.literal(this.itemName));
            }
        } else if (baseClone.hasCustomHoverName()) {
            b1 = 1;
            i += b1;
            baseClone.resetHoverName();
        }

        cost = j + i;
        if (i <= 0) {
            baseClone = ItemStack.empty();
        }

        if (b1 == i && b1 > 0 && cost >= 40) {
            cost = 39;
        }

        if (cost >= 40 && player.getGameMode() != GameMode.CREATIVE) {
            baseClone = ItemStack.empty();
        }

        if (!baseClone.isEmpty()) {
            int repairCost = getRepairCost(baseClone);

            if (!addClone.isEmpty() && repairCost < getRepairCost(addClone)) {
                repairCost = getRepairCost(addClone);
            }

            if (b1 != i || b1 == 0) {
                repairCost = repairCost * 2 + 1;
            }

            setRepairCost(baseClone, repairCost);
            EnchantUtils.setEnchantments(baseEnchants, baseClone);
        }

        return new AnvilResult(baseClone, cost, repairItemCountCost);
    }

    public static int calculateIncreasedRepairCost(int i) {
        return i * 2 + 1;
    }

    private int getRepairCost(ItemStack stack) {
        if (stack instanceof Repairable repairable) {
            return repairable.getRepairCost();
        } else {
            return 0;
        }
    }

    private void setRepairCost(ItemStack stack, int cost) {
        if (stack.getItemMeta() instanceof Repairable repairable) {
            repairable.setRepairCost(cost);
            stack.setItemMeta(repairable);
        }
    }

    private int getDamageSafe(ItemStack stack) {
        if (stack instanceof Damageable damageable) {
            return damageable.getDamage();
        }

        return 0;
    }

    private String getName(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return getName(stack.getType());
        }

        Component nameComponent = meta.displayName();
        if (nameComponent == null) {
            return getName(stack.getType());
        }

        // TODO: Check if this is a translatable component and run it past vanilla language file (see TODO below)
        return nameComponent.toString();
    }

    private String getName(Material material) {
        // TODO: Add vanilla translation util to WbsUtils, similar to what's in WbsChatGame -- user can upload
        //  a language file that gets read to allow translation of vanilla keys

        // In the meantime, just return our best.
        if (material == Material.HAY_BLOCK) {
            return "Hay Bale"; // hate this :(
        }

        return WbsEnums.toPrettyString(material);
    }
}