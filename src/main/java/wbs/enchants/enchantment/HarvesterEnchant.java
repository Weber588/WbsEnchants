package wbs.enchants.enchantment;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Fire;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.BlockQuery;

import java.util.Collection;
import java.util.List;

public class HarvesterEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "When you break or right click a crop, a 3x3 area is harvested " +
            "and replanted automatically. Width increases by 2 per level.";

    public static List<Material> UNHARVESTABLE_CROPS = List.of(
            Material.PITCHER_CROP,
            Material.TORCHFLOWER_CROP,
            Material.PUMPKIN_STEM,
            Material.MELON_STEM,
            Material.BAMBOO
    );

    public HarvesterEnchant() {
        super("harvester", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
                .supportedItems(ItemTypeTagKeys.HOES)
                .targetDescription("Hoe")
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(5, 8))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(55, 8));
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

        if (isEnchantmentOn(item)) {
            if (crop.getAge() == crop.getMaximumAge()) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    event.setCancelled(true);
                }

                int level = getLevel(item);
                List<Block> cropBlocks = new BlockQuery()
                        .setPredicate(HarvesterEnchant::isHarvestableCrop)
                        .setMaxDistance(level)
                        .getSquare(clickedBlock, BlockFace.DOWN);

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
}
