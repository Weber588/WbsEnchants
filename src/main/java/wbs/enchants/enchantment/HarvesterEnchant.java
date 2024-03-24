package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Fire;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.BlockQueryUtils;

import java.util.Collection;
import java.util.List;

public class HarvesterEnchant extends WbsEnchantment {
    public static List<Material> UNHARVESTABLE_CROPS = List.of(
            Material.PITCHER_CROP,
            Material.TORCHFLOWER_CROP,
            Material.PUMPKIN_STEM,
            Material.MELON_STEM,
            Material.BAMBOO
    );

    public HarvesterEnchant() {
        super("harvester");
    }

    @EventHandler
    public void onHarvest(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        Ageable crop = getCrop(clickedBlock);
        if (crop == null) {
            return;
        }
        
        if (BlockChanger.isPlayerBreaking(player, clickedBlock)) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (containsEnchantment(item)) {
            if (crop.getAge() == crop.getMaximumAge()) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    event.setCancelled(true);
                }

                int level = getLevel(item);
                List<Block> cropBlocks = BlockQueryUtils.getSquareMatching(clickedBlock, level, BlockFace.DOWN,
                                        HarvesterEnchant::isHarvestableCrop);

                harvestCrop(player, clickedBlock);

                BlockChanger.prepare(cropBlocks)
                        .setMatching(HarvesterEnchant::isHarvestableCrop)
                        .run(player, block -> HarvesterEnchant.harvestCrop(player, block));
            }
        }
    }

    private static boolean harvestCrop(Player player, Block block) {
        Ageable updateCrop = getCrop(block);

        if (updateCrop == null) {
            return false;
        }

        Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand(), player);

        Material placementMaterial = updateCrop.getPlacementMaterial();
        boolean foundPlacement = false;
        for (ItemStack drop : drops) {
            if (drop.getType() == placementMaterial) {
                drop.setAmount(drop.getAmount() - 1);
                foundPlacement = true;
                break;
            }
        }

        if (foundPlacement) {
            updateCrop.setAge(0);
            drops.forEach(item -> block.getWorld().dropItemNaturally(block.getLocation(), item));
            block.setBlockData(updateCrop);
        } else {
            player.breakBlock(block);
        }

        return true;
    }

    public static boolean isHarvestableCrop(Block block) {
        Ageable checkCrop = getCrop(block);

        if (checkCrop == null) {
            return false;
        }

        if (UNHARVESTABLE_CROPS.contains(block.getType())) {
            return false;
        }

        return checkCrop.getAge() == checkCrop.getMaximumAge();
    }

    @Nullable
    @Contract("null -> null")
    public static Ageable getCrop(Block block) {
        if (block == null) {
            return null;
        }

        if (!(block.getBlockData() instanceof Ageable crop)) {
            return null;
        }

        if (crop instanceof Fire) {
            return null;
        }

        return crop;
    }

    @Override
    public String getDisplayName() {
        return "&7Harvester";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.COMMON;
    }

    @Override
    public int getMaxLevel() {
        return 2;
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
    public @NotNull String getDescription() {
        int maxWidth = getMaxLevel() * 2 + 1;
        return "When you break or right click a crop, a 3x3 area is harvested and replanted automatically. Width " +
                "increases by 2 per level, up to a maximum of " + maxWidth + " at level " + getMaxLevel() + ".";
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Hoe";
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return Tag.ITEMS_HOES.isTagged(itemStack.getType());
    }
}
