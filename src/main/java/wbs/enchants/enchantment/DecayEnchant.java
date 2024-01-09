package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.MaterialUtils;
import wbs.utils.util.WbsMath;

import java.util.Collection;
import java.util.List;

public class DecayEnchant extends AbstractDamageEnchant {
    public DecayEnchant() {
        super("decay");
    }

    @Override
    public @NotNull String getDescription() {
        return "Has a chance to drop bonemeal when breaking compostable blocks, and does increased damage against " +
                "living mobs (especially illagers).";
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (containsEnchantment(item)) {
            Collection<ItemStack> possibleDrops = broken.getDrops(item);
            for (ItemStack drop : possibleDrops) {
                if (drop.getType() == broken.getBlockData().getPlacementMaterial()) {
                    return;
                }
            }

            double compostChance = MaterialUtils.getCompostChance(broken.getType());

            int level = getLevel(item);

            double leveledChance = 1 - Math.pow(1 - (compostChance / 100), level);

            if (!WbsMath.chance(leveledChance * 100)) {
                return;
            }

            broken.getWorld().dropItemNaturally(broken.getLocation(), new ItemStack(Material.BONE_MEAL));
        }
    }

    @Override
    @EventHandler
    public void catchEvent(EntityDamageByEntityEvent event) {
        onDamage(event);
    }

    @Override
    protected void handleAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity attacker, @NotNull Entity victim, @Nullable Projectile projectile) {
        if (!(victim instanceof LivingEntity livingVictim)) {
            return;
        }

        EntityCategory category = livingVictim.getCategory();
        if (category == EntityCategory.UNDEAD) {
            return;
        }

        EntityEquipment equipment = attacker.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack item = equipment.getItemInMainHand();
        if (containsEnchantment(item)) {
            int level = getLevel(item);
            double damage = event.getDamage();

            if (category == EntityCategory.ILLAGER) {
                damage += 2 * level; // (nerfed) Smite-like calculation
            } else {
                damage += (0.5 * level) + 0.5; // Sharpness-like calculation
            }

            event.setDamage(damage);
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Decay";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.VERY_RARE;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return enchantment == SILK_TOUCH ||
                // Conflict with Sharpness, and everything it conflicts with
                enchantment == DAMAGE_ALL ||
                DAMAGE_ALL.conflictsWith(enchantment);
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return Tag.ITEMS_HOES.isTagged(itemStack.getType());
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Hoe";
    }
}
