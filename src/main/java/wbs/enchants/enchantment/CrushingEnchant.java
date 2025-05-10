package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.BlockDropEnchantment;
import wbs.utils.util.WbsEnums;

import java.util.*;

public class CrushingEnchant extends WbsEnchantment implements BlockDropEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A tool enchantment that crushes blocks into other, " +
            "more broken-down versions!";
    private static final String CRUSH_MAP_KEY = "crush-materials";

    private final Map<Material, CrushDefinition> crushingMap = new HashMap<>();

    public CrushingEnchant() {
        super("crushing", DEFAULT_DESCRIPTION);

        getDefinition()
                .weight(5)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_MINING)
                .exclusiveInject(EnchantmentTagKeys.EXCLUSIVE_SET_MINING);
    }

    @Override
    public EventPriority getDropPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public void configure(ConfigurationSection section, String directory) {
        super.configure(section, directory);

        ConfigurationSection mapSection = section.getConfigurationSection(CRUSH_MAP_KEY);
        String mapDir = directory + "/" + CRUSH_MAP_KEY;
        if (mapSection != null) {
            for (String key : mapSection.getKeys(false)) {
                Material from = WbsEnums.getEnumFromString(Material.class, key);

                if (from == null) {
                    WbsEnchants.getInstance().settings
                            .logError("Invalid material: " + key + ".", mapDir + "/" + key);
                    continue;
                }

                CrushDefinition definition;

                if (mapSection.isConfigurationSection(key)) {
                    ConfigurationSection outputSection = mapSection.getConfigurationSection(key);
                    if (outputSection == null) {
                        WbsEnchants.getInstance().settings
                                .logError("Section must be a material name or section: " + key + ".",
                                        mapDir + "/" + key);
                        continue;
                    }

                    final String DEFAULT_TO_NAME = "to";

                    String toString = outputSection.getString("material");
                    toString = outputSection.getString(DEFAULT_TO_NAME, toString);

                    if (toString == null) {
                        WbsEnchants.getInstance().settings
                                .logError("Section must contain a key \"" + DEFAULT_TO_NAME +
                                                "\" mapping to a material: " + key + ".",
                                        mapDir + "/" + key);
                        continue;
                    }

                    Material to = WbsEnums.getEnumFromString(Material.class, toString);
                    if (to == null) {
                        WbsEnchants.getInstance().settings
                                .logError("Invalid material: " + toString + ".", mapDir + "/" + DEFAULT_TO_NAME);
                        continue;
                    }

                    int minAmount = outputSection.getInt("amount", -1);
                    minAmount = outputSection.getInt("min-amount", minAmount);

                    if (minAmount == -1) {
                        minAmount = 1;
                    }

                    int maxAmount = outputSection.getInt("amount", -1);
                    maxAmount = outputSection.getInt("max-amount", maxAmount);

                    if (maxAmount == -1) {
                        maxAmount = 1;
                    }

                    definition = new CrushDefinition(to, minAmount, maxAmount);
                } else {
                    String toString = mapSection.getString(key, String.valueOf(mapSection.get(key)));
                    Material to = WbsEnums.getEnumFromString(Material.class, toString);
                    if (to == null) {
                        WbsEnchants.getInstance().settings
                                .logError("Invalid material: " + toString + ".", mapDir + "/" + key);
                        continue;
                    }
                    definition = new CrushDefinition(to);
                }

                crushingMap.put(from, definition);
            }
        }

        if (crushingMap.isEmpty()) {
            populateDefaultCrushingMap();
        }
    }

    private void populateDefaultCrushingMap() {
        addCrushDef(Material.STONE, Material.COBBLESTONE); // As normal
        addCrushDef(Material.COBBLESTONE, Material.GRAVEL);
        addCrushDef(Material.GRAVEL, Material.SAND);

        addCrushDef(Material.SANDSTONE, Material.SAND);
        addCrushDef(Material.CHISELED_SANDSTONE, Material.SAND);
        addCrushDef(Material.CUT_SANDSTONE, Material.SAND);
        addCrushDef(Material.SMOOTH_SANDSTONE, Material.SAND);

        addCrushDef(Material.RED_SANDSTONE, Material.RED_SAND);
        addCrushDef(Material.CHISELED_RED_SANDSTONE, Material.RED_SAND);
        addCrushDef(Material.CUT_RED_SANDSTONE, Material.RED_SAND);
        addCrushDef(Material.SMOOTH_RED_SANDSTONE, Material.RED_SAND);

        addCrushDef(Material.STONE_BRICKS, Material.CRACKED_STONE_BRICKS);
        addCrushDef(Material.DEEPSLATE_BRICKS, Material.CRACKED_DEEPSLATE_BRICKS);
        addCrushDef(Material.DEEPSLATE_TILES, Material.CRACKED_DEEPSLATE_TILES);
        addCrushDef(Material.POLISHED_BLACKSTONE_BRICKS, Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);

        addCrushDef(Material.AMETHYST_BLOCK, Material.AMETHYST_SHARD, 1, 3);
        addCrushDef(Material.BONE_BLOCK, Material.BONE_MEAL, 9);
        addCrushDef(Material.COAL_BLOCK, Material.COAL, 9);
        addCrushDef(Material.COPPER_BLOCK, Material.COPPER_INGOT, 9);
        addCrushDef(Material.GOLD_BLOCK, Material.GOLD_INGOT, 9);
        addCrushDef(Material.IRON_BLOCK, Material.IRON_INGOT, 9);
        addCrushDef(Material.DIAMOND_BLOCK, Material.DIAMOND, 9);
        addCrushDef(Material.EMERALD_BLOCK, Material.EMERALD, 9);
        addCrushDef(Material.NETHERITE_BLOCK, Material.NETHERITE_INGOT, 9);
        addCrushDef(Material.HAY_BLOCK, Material.WHEAT, 8, 9);
        addCrushDef(Material.NETHER_WART_BLOCK, Material.NETHER_WART, 3, 9);
        addCrushDef(Material.RAW_COPPER_BLOCK, Material.RAW_COPPER, 7, 9);
        addCrushDef(Material.RAW_IRON_BLOCK, Material.RAW_IRON, 7, 9);
        addCrushDef(Material.RAW_GOLD_BLOCK, Material.RAW_GOLD, 7, 9);

        addCrushDef(Material.BRICKS, Material.BRICK, 3, 4);
        addCrushDef(Material.NETHER_BRICKS, Material.NETHER_BRICK, 3, 4);

        addCrushDef(Material.PRISMARINE_BRICKS, Material.PRISMARINE);
        addCrushDef(Material.DARK_PRISMARINE, Material.PRISMARINE);
        addCrushDef(Material.PRISMARINE, Material.PRISMARINE_SHARD, 3, 4);
        addCrushDef(Material.SEA_LANTERN, Material.PRISMARINE_CRYSTALS, 2, 5);

        addCrushDef(Material.QUARTZ_BLOCK, Material.QUARTZ, 3, 4);
        addCrushDef(Material.QUARTZ_BRICKS, Material.QUARTZ, 3, 4);
        addCrushDef(Material.QUARTZ_PILLAR, Material.QUARTZ, 3, 4);
        addCrushDef(Material.SMOOTH_QUARTZ, Material.QUARTZ, 3, 4);

        addCrushDef(Material.WHITE_CONCRETE, Material.WHITE_CONCRETE_POWDER);
        addCrushDef(Material.RED_CONCRETE, Material.RED_CONCRETE_POWDER);
        addCrushDef(Material.ORANGE_CONCRETE, Material.ORANGE_CONCRETE_POWDER);
        addCrushDef(Material.YELLOW_CONCRETE, Material.YELLOW_CONCRETE_POWDER);
        addCrushDef(Material.LIME_CONCRETE, Material.LIME_CONCRETE_POWDER);
        addCrushDef(Material.GREEN_CONCRETE, Material.GREEN_CONCRETE_POWDER);
        addCrushDef(Material.BLUE_CONCRETE, Material.BLUE_CONCRETE_POWDER);
        addCrushDef(Material.CYAN_CONCRETE, Material.CYAN_CONCRETE_POWDER);
        addCrushDef(Material.LIGHT_BLUE_CONCRETE, Material.LIGHT_BLUE_CONCRETE_POWDER);
        addCrushDef(Material.PINK_CONCRETE, Material.PINK_CONCRETE_POWDER);
        addCrushDef(Material.MAGENTA_CONCRETE, Material.MAGENTA_CONCRETE_POWDER);
        addCrushDef(Material.PURPLE_CONCRETE, Material.PURPLE_CONCRETE_POWDER);
        addCrushDef(Material.LIGHT_GRAY_CONCRETE, Material.LIGHT_GRAY_CONCRETE_POWDER);
        addCrushDef(Material.GRAY_CONCRETE, Material.GRAY_CONCRETE_POWDER);
        addCrushDef(Material.BLACK_CONCRETE, Material.BLACK_CONCRETE_POWDER);
    }

    private void addCrushDef(Material from, Material to) {
        addCrushDef(from, to, 1);
    }
    private void addCrushDef(Material from, Material to, int amount) {
        addCrushDef(from, to, amount, amount);
    }
    private void addCrushDef(Material from, Material to, int minAmount, int maxAmount) {
        crushingMap.put(from, new CrushDefinition(to, minAmount, maxAmount));
    }

    @Override
    public ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile) {
        ConfigurationSection section = super.buildConfigurationSection(baseFile);

        if (crushingMap.isEmpty()) {
            populateDefaultCrushingMap();
        }

        for (Map.Entry<Material, CrushDefinition> entry : crushingMap.entrySet()) {
            String baseKey = CRUSH_MAP_KEY + "." + entry.getKey().name();
            CrushDefinition def = entry.getValue();
            if (def.minAmount == 1 && def.maxAmount == 1) {
                section.set(baseKey, def.to.name());
            } else {
                section.set(baseKey + ".to", def.to.name());
                section.set(baseKey + ".minAmount", def.minAmount);
                section.set(baseKey + ".maxAmount", def.maxAmount);
            }
        }

        return section;
    }

    @Override
    public void apply(BlockDropItemEvent event, MarkedLocation marked) {
        BlockDropEnchantment.removePendingDrop(marked);

        List<Item> items = event.getItems();
        List<ItemStack> toDrop = new LinkedList<>();

        for (Item item : items) {
            ItemStack stack = item.getItemStack();
            CrushDefinition def = crushingMap.get(stack.getType());

            if (def != null) {
                toDrop.addAll(def.produce(stack));
            } else {
                toDrop.add(item.getItemStack());
            }
        }

        Block block = event.getBlock();
        World world = block.getWorld();

        event.setCancelled(true);

        for (ItemStack toSpawn : condenseItems(toDrop)) {
            world.dropItemNaturally(block.getLocation(), toSpawn);
        }
    }

    @Contract(pure = true)
    private List<ItemStack> condenseItems(List<ItemStack> stacks) {
        // Don't modify original list
        stacks = new LinkedList<>(stacks);

        List<ItemStack> condensed = new LinkedList<>();

        while (!stacks.isEmpty()) {
            ItemStack toMove = stacks.getFirst();
            stacks.removeFirst();

            List<ItemStack> toAdd = new LinkedList<>();

            boolean foundMatch = false;
            for (ItemStack toCompare : condensed) {
                int stackSize = toCompare.getMaxStackSize();
                int existingAmount = toCompare.getAmount();

                int remaining = stackSize - existingAmount;
                if (remaining > 0) {
                    if (toCompare.isSimilar(toMove)) {
                        foundMatch = true;
                        stacks.remove(toMove);

                        int availableAmount = toMove.getAmount();
                        if (availableAmount <= remaining) {
                            toCompare.setAmount(existingAmount + availableAmount);
                        } else {
                            toCompare.setAmount(stackSize);
                            availableAmount -= remaining;

                            ItemStack moveClone = toMove.clone();
                            moveClone.setAmount(availableAmount);
                            toAdd.add(moveClone);
                        }
                    }
                }
            }

            if (foundMatch) {
                condensed.addAll(toAdd);
            } else {
                condensed.add(toMove.clone());
            }
        }

        return condensed;
    }

    private static class CrushDefinition {
        private final Material to;
        private final int minAmount;
        private final int maxAmount;

        private CrushDefinition(Material to) {
            this(to, 1);
        }

        private CrushDefinition(Material to, int amount) {
            this(to, amount, amount);
        }

        private CrushDefinition(Material to, int minAmount, int maxAmount) {
            this.to = to;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }

        public List<ItemStack> produce(ItemStack from) {
            int amount = 0;
            if (maxAmount == minAmount) {
                amount = maxAmount * from.getAmount();
            } else {
                for (int i = 0; i < from.getAmount(); i++) {
                    amount += new Random().nextInt(maxAmount - minAmount) + minAmount;
                }
            }

            List<ItemStack> produced = new LinkedList<>();

            while (amount > to.getMaxStackSize()) {
                produced.add(new ItemStack(to, from.getMaxStackSize()));
                amount -= from.getMaxStackSize();
            }

            if (amount > 0) {
                produced.add(new ItemStack(to, amount));
            }
            return produced;
        }
    }
}
