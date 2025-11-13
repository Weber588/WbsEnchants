package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.Bisected;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantsBootstrapSettings;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.enchants.util.BlockQuery;
import wbs.utils.util.MapStream;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class BloomingEnchant extends WbsEnchantment implements TickableEnchant {
    private static final String DEFAULT_DESCRIPTION = "The item passively regains durability when near flowers, with " +
            "speed depending on how many types are around. Rarer types repair faster!";

    private static final double DEFAULT_SMALL_FLOWER_POWER = 0.1;
    // Double high already count twice, so lower individual, but higher overall
    private static final double DEFAULT_LARGE_FLOWER_POWER = 0.12;

    private static final double DEFAULT_FLOWER_POT_POWER = DEFAULT_SMALL_FLOWER_POWER / 2;

    // TODO: Make this configurable
    private static final Map<TypedKey<BlockType>, Double> FLOWER_POWERS = new HashMap<>();

    static {
        FLOWER_POWERS.put(BlockTypeKeys.WITHER_ROSE, DEFAULT_SMALL_FLOWER_POWER * 5);
        FLOWER_POWERS.put(BlockTypeKeys.POTTED_WITHER_ROSE, DEFAULT_FLOWER_POT_POWER * 5);

        FLOWER_POWERS.put(BlockTypeKeys.TORCHFLOWER, DEFAULT_SMALL_FLOWER_POWER * 5);
        FLOWER_POWERS.put(BlockTypeKeys.POTTED_TORCHFLOWER, DEFAULT_FLOWER_POT_POWER * 5);

        FLOWER_POWERS.put(BlockTypeKeys.PITCHER_PLANT, DEFAULT_LARGE_FLOWER_POWER * 5);

        FLOWER_POWERS.put(BlockTypeKeys.SPORE_BLOSSOM, DEFAULT_SMALL_FLOWER_POWER * 3);
        FLOWER_POWERS.put(BlockTypeKeys.FLOWERING_AZALEA, DEFAULT_SMALL_FLOWER_POWER);
        FLOWER_POWERS.put(BlockTypeKeys.FLOWERING_AZALEA_LEAVES, DEFAULT_SMALL_FLOWER_POWER / 2);
    }

    private Map<BlockType, Double> getFlowerPowers() {
        Registry<@NotNull BlockType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK);

        Map<BlockType, Double> flowerPowers = new HashMap<>(new MapStream<>(this.flowerPowers)
                .mapKey(registry::get)
                .toMap()
        );

        new MapStream<>(tagFlowerPowers)
                .mapKey(registry::getTag)
                .mapKey(tag -> tag.resolve(registry))
                .forEachOrdered(entry -> {
                    entry.getKey().forEach(blockType -> {
                        flowerPowers.putIfAbsent(blockType, entry.getValue());
                    });
                });

        return flowerPowers;
    }

    private final Map<TypedKey<BlockType>, Double> flowerPowers = new HashMap<>();
    private final Map<TagKey<BlockType>, Double> tagFlowerPowers = new LinkedHashMap<>();
    private double uniqueFlowerMultiplier = 2;

    public BloomingEnchant() {
        super("blooming", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_DURABILITY)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_SELF_REPAIRING);
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        super.configure(section, directory);

        RegistryKey<BlockType> registryKey = RegistryKey.BLOCK;

        uniqueFlowerMultiplier = section.getDouble("unique-flower-multiplier", 1);

        ConfigurationSection flowerPowersSection = section.getConfigurationSection("flower-powers");
        if (flowerPowersSection != null) {
            String flowerDirectory = directory + "/flower-powers";
            for (String keyString : flowerPowersSection.getKeys(false)) {
                boolean isTag = keyString.startsWith("#");

                NamespacedKey key;
                if (isTag) {
                    key = NamespacedKey.fromString(keyString.substring(1));
                } else {
                    key = NamespacedKey.fromString(keyString);
                }

                if (key == null) {
                    EnchantsBootstrapSettings.getInstance().logError("Invalid key: " + keyString, flowerDirectory + "/" + keyString);
                    System.out.println("Invalid key: " + keyString + " (" + flowerDirectory + "/" + keyString + ")");
                    continue;
                }

                if (!flowerPowersSection.isDouble(keyString)) {
                    EnchantsBootstrapSettings.getInstance().logError("Must be a number: " + flowerPowersSection.getString(keyString), flowerDirectory + "/" + keyString);
                    System.out.println("Must be a number: " + flowerPowersSection.getString(keyString) + " (" + flowerDirectory + "/" + keyString + ")");
                    continue;
                }

                double power = flowerPowersSection.getDouble(keyString);

                if (isTag) {
                    TagKey<BlockType> tagKey = TagKey.create(registryKey, key);
                    tagFlowerPowers.put(tagKey, power);
                } else {
                    TypedKey<BlockType> typedKey = TypedKey.create(registryKey, key);
                    flowerPowers.put(typedKey, power);
                }
            }
        }
    }

    @Override
    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection section = super.buildConfigurationSection(baseFile);

        FLOWER_POWERS.forEach((key, power) -> {
            section.set("flower-powers." + key.key().asMinimalString(), power);
        });

        section.set("flower-powers.#minecraft:small_flowers", 0.1);
        section.set("flower-powers.#minecraft:flowers", 0.12);
        section.set("flower-powers.#minecraft:flower_pots", 0.05);

        return section;
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

        Integer damage = item.getData(DataComponentTypes.DAMAGE);

        if (damage == null || damage == 0) {
            return;
        }
        Map<BlockType, Double> flowerPowers = getFlowerPowers();

        List<Block> nearbyFlowers = new BlockQuery()
                .setMaxDistance(5)
                .setPredicate(block -> {
                    BlockType blockType = block.getType().asBlockType();
                    return flowerPowers.containsKey(blockType);
                })
                .setDistanceMode(BlockQuery.DistanceMode.MANHATTAN)
                .getNearby(player.getLocation().getBlock());

        double totalFlowerPower = 0;

        Set<BlockType> nearbyFlowerTypes = new HashSet<>();

        for (Block block : nearbyFlowers) {
            BlockType blockType = Objects.requireNonNull(block.getType().asBlockType());

            double powerMultiplier = 1;

            // New flower types count double to encourage mixing flowers
            if (!nearbyFlowerTypes.contains(blockType)) {
                powerMultiplier *= uniqueFlowerMultiplier;
            }

            if (block.getBlockData() instanceof Bisected) {
                powerMultiplier *= 0.5;
            }

            nearbyFlowerTypes.add(blockType);

            Double power = flowerPowers.get(blockType);

            if (power != null) {
                totalFlowerPower += power * powerMultiplier;
            }
        }

        int toRestore = (int) Math.ceil(totalFlowerPower);
        item.setData(DataComponentTypes.DAMAGE, Math.max(0, damage - toRestore));
    }
}
