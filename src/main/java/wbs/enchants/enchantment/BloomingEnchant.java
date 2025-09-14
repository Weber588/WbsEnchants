package wbs.enchants.enchantment;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.Bisected;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.enchants.util.BlockQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BloomingEnchant extends WbsEnchantment implements TickableEnchant {
    private static final String DEFAULT_DESCRIPTION = "The item passively regains durability when near flowers, with " +
            "speed depending on how many types are around. Certain types heal faster!";

    private static final double DEFAULT_SMALL_FLOWER_POWER = 0.75;;
    // Double high already count twice, so lower individual, but higher overall
    private static final double DEFAULT_LARGE_FLOWER_POWER = 0.4;;

    private static final double DEFAULT_FLOWER_POT_POWER = DEFAULT_SMALL_FLOWER_POWER / 2;

    // TODO: Make this configurable
    private static final Map<BlockType, Double> FLOWER_POWERS = new HashMap<>();

    static {
        FLOWER_POWERS.put(BlockType.WITHER_ROSE, DEFAULT_SMALL_FLOWER_POWER * 2);
        FLOWER_POWERS.put(BlockType.POTTED_WITHER_ROSE, DEFAULT_FLOWER_POT_POWER * 2);

        FLOWER_POWERS.put(BlockType.TORCHFLOWER, DEFAULT_SMALL_FLOWER_POWER * 3);
        FLOWER_POWERS.put(BlockType.POTTED_TORCHFLOWER, DEFAULT_FLOWER_POT_POWER * 3);

        FLOWER_POWERS.put(BlockType.PITCHER_PLANT, DEFAULT_LARGE_FLOWER_POWER * 3);

        FLOWER_POWERS.put(BlockType.SPORE_BLOSSOM, DEFAULT_SMALL_FLOWER_POWER * 3);
        FLOWER_POWERS.put(BlockType.FLOWERING_AZALEA, DEFAULT_SMALL_FLOWER_POWER);
        FLOWER_POWERS.put(BlockType.FLOWERING_AZALEA_LEAVES, DEFAULT_SMALL_FLOWER_POWER / 2);
    }

    public BloomingEnchant() {
        super("blooming", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_DURABILITY);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        Registry<BlockType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK);

        registry.getTag(BlockTypeTagKeys.FLOWERS).forEach(key -> {
            BlockType type = registry.get(key);
            if (type != null && !FLOWER_POWERS.containsKey(type)) {
                if (type.createBlockData() instanceof Bisected) {
                    FLOWER_POWERS.put(type, DEFAULT_LARGE_FLOWER_POWER);
                } else {
                    FLOWER_POWERS.put(type, DEFAULT_SMALL_FLOWER_POWER);
                }
            }
        });

        registry.getTag(BlockTypeTagKeys.FLOWER_POTS).forEach(key -> {
            BlockType type = registry.get(key);
            if (type != null && !FLOWER_POWERS.containsKey(type)) {
                FLOWER_POWERS.put(type, DEFAULT_FLOWER_POT_POWER);
            }
        });
    }

    @Override
    public int getTickFrequency() {
        return 10;
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

        double totalFlowerPower = nearbyFlowers.stream()
                .mapToDouble(block -> FLOWER_POWERS.get(block.getType().asBlockType()))
                .sum();

        int toRestore = (int) (totalFlowerPower / 7);
        damageable.setDamage(Math.max(0, damageable.getDamage() - toRestore));

        item.setItemMeta(damageable);
    }
}
