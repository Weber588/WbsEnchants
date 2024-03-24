package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsItems;

import java.util.Map;
import java.util.Set;

public class LavaWalkerEnchant extends WbsEnchantment {
    private static final Map<Material, Integer> REPLACEMENT_BLOCKS = Map.of(
            Material.OBSIDIAN, 9,
            Material.CRYING_OBSIDIAN, 1
    );

    public LavaWalkerEnchant() {
        super("lava_walker");
    }

    @EventHandler
    public void onMovement(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        Player player = event.getPlayer();
        // Kind of eh way to check if player is on the ground - if they're within 0.05 blocks of the top of a
        // full block, good enough.
        if (player.getLocation().getY() % 1.0 >= 0.05) {
            return;
        }

        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return;
        }

        Location from = event.getFrom();
        if (from.getBlock().equals(to.getBlock())) {
            return;
        }

        ItemStack boots = equipment.getBoots();
        if (boots != null && containsEnchantment(boots)) {
            int level = getLevel(boots);

            int radius = Math.max(1, level) + 2;
            int radiusSquared = radius * radius;

            Location central = to.clone().add(BlockFace.DOWN.getDirection());
            int centralX = central.getBlockX();
            int centralY = central.getBlockY();
            int centralZ = central.getBlockZ();

            World world = player.getWorld();

            int changed = 0;

            for (int x = -radius; x < radius; x++) {
                for (int z = -radius; z < radius; z++) {
                    if ((x * x) + (z * z) <= radiusSquared) {
                        Block check = world.getBlockAt(centralX + x, centralY, centralZ + z);
                        if (check.getType() == Material.LAVA) {
                            check.setType(WbsCollectionUtil.getRandomWeighted(REPLACEMENT_BLOCKS));
                            changed++;
                        }
                    }
                }
            }

            if (changed > 0) {
                WbsItems.damageItem(player, boots, 1, EquipmentSlot.FEET);
            }
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "Frost walker... but for lava!";
    }

    @Override
    public String getDisplayName() {
        return "&7Lava Walker";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_FEET;
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
    public Set<Enchantment> getIndirectConflicts() {
        return Set.of(FROST_WALKER);
    }
}
