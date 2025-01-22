package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.enchants.util.BlockQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloomingEnchant extends WbsEnchantment implements TickableEnchant {
    private static final String DEFAULT_DESCRIPTION = "The item passively regains durability when near flowers, with " +
            "speed depending on how many types are around. Certain types heal faster!";

    // TODO: Make this configurable
    private static final Map<BlockType, Double> FLOWER_POWERS = new HashMap<>();

    static {
        FLOWER_POWERS.put(BlockType.ALLIUM, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_ALLIUM, 0.5);
        FLOWER_POWERS.put(BlockType.AZURE_BLUET, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_AZURE_BLUET, 0.5);
        FLOWER_POWERS.put(BlockType.BLUE_ORCHID, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_BLUE_ORCHID, 0.5);
        FLOWER_POWERS.put(BlockType.CORNFLOWER, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_CORNFLOWER, 0.5);
        FLOWER_POWERS.put(BlockType.DANDELION, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_DANDELION, 0.5);
        FLOWER_POWERS.put(BlockType.LILY_OF_THE_VALLEY, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_LILY_OF_THE_VALLEY, 0.5);
        FLOWER_POWERS.put(BlockType.ORANGE_TULIP, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_ORANGE_TULIP, 0.5);
        FLOWER_POWERS.put(BlockType.OXEYE_DAISY, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_OXEYE_DAISY, 0.5);
        FLOWER_POWERS.put(BlockType.PINK_TULIP, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_PINK_TULIP, 0.5);
        FLOWER_POWERS.put(BlockType.POPPY, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_POPPY, 0.5);
        FLOWER_POWERS.put(BlockType.RED_TULIP, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_RED_TULIP, 0.5);
        FLOWER_POWERS.put(BlockType.TORCHFLOWER, 3.0);
        FLOWER_POWERS.put(BlockType.POTTED_TORCHFLOWER, 1.5);
        FLOWER_POWERS.put(BlockType.WHITE_TULIP, 1.0);
        FLOWER_POWERS.put(BlockType.POTTED_WHITE_TULIP, 0.5);
        FLOWER_POWERS.put(BlockType.WITHER_ROSE, 2.0);
        FLOWER_POWERS.put(BlockType.POTTED_WITHER_ROSE, 1.0);

        // Double high already count twice, so lower individual, but higher overall
        FLOWER_POWERS.put(BlockType.LILAC, 0.6);
        FLOWER_POWERS.put(BlockType.PEONY, 0.6);
        FLOWER_POWERS.put(BlockType.PITCHER_PLANT, 3.0);
        FLOWER_POWERS.put(BlockType.ROSE_BUSH, 0.6);
        FLOWER_POWERS.put(BlockType.SUNFLOWER, 0.6);

        FLOWER_POWERS.put(BlockType.SPORE_BLOSSOM, 3.0);
        FLOWER_POWERS.put(BlockType.FLOWERING_AZALEA, 1.0);
        FLOWER_POWERS.put(BlockType.FLOWERING_AZALEA_LEAVES, 0.5);
    }

    public BloomingEnchant() {
        super("blooming", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_DURABILITY);
    }

    @Override
    public int getTickFrequency() {
        return 40;
    }

    @Override
    public void onTickEquipped(LivingEntity owner, ItemStack item, EquipmentSlot slot) {
        if (!(owner instanceof Player player)) {
            return;
        }

        if (!player.isOnline()) {
            return;
        }

        if (!(item.getItemMeta() instanceof Damageable damageable) || !damageable.hasDamageValue()) {
            return;
        }

        if (damageable.getDamage() == 0) {
            return;
        }

        List<Block> nearbyFlowers = new BlockQuery()
                .setMaxDistance(5)
                .setPredicate(block -> FLOWER_POWERS.containsKey(block.getType().asBlockType()))
                .setDistanceMode(BlockQuery.DistanceMode.MANHATTAN)
                .getNearby(player.getLocation().getBlock());

        WbsEnchants.getInstance().sendMessage("nearbyFlowers: " + nearbyFlowers.size(), owner);

        double totalFlowerPower = nearbyFlowers.stream()
                .mapToDouble(block -> FLOWER_POWERS.get(block.getType().asBlockType()))
                .sum();

        WbsEnchants.getInstance().sendMessage("Flower power: " + totalFlowerPower, owner);
        int toRestore = (int) (totalFlowerPower / 7);
        damageable.setDamage(Math.max(0, damageable.getDamage() - toRestore));

        item.setItemMeta(damageable);
    }
}
