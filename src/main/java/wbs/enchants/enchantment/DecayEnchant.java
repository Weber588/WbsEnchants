package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.TargetedDamageEnchant;
import wbs.enchants.util.EntityUtils;
import wbs.enchants.util.MaterialUtils;
import wbs.utils.util.WbsMath;

import java.util.Collection;
import java.util.Set;

public class DecayEnchant extends TargetedDamageEnchant {
    private static final String DEFAULT_DESCRIPTION = "Has a chance to drop bonemeal when breaking compostable blocks, " +
            "and does increased damage against living mobs (especially illagers).";

    public DecayEnchant() {
        super("decay", DEFAULT_DESCRIPTION);
        maxLevel = 5;
        supportedItems = ItemTypeTagKeys.HOES;
        weight = 5;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Decay";
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (isEnchantmentOn(item)) {
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
    protected @NotNull Set<EntityType> getDefaultMobs() {
        return Set.of(EntityType.EVOKER, EntityType.ILLUSIONER, EntityType.PILLAGER, EntityType.VINDICATOR);
    }

    @Override
    protected boolean shouldAffect(Entity victim) {
        return super.shouldAffect(victim) || (
                victim instanceof LivingEntity livingVictim &&
                        !EntityUtils.UNDEAD.isTagged(livingVictim.getType())
        );
    }

    @Override
    protected double getBonusDamage(Entity victim) {
        if (victim instanceof Illager) {
            return 2;
        } else if (EntityUtils.UNDEAD.isTagged(victim.getType())) {
            return 0;
        }

        return 0.4;
    }
}
