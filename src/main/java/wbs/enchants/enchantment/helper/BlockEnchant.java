package wbs.enchants.enchantment.helper;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.events.EnchantedBlockBreakEvent;
import wbs.enchants.events.EnchantedBlockPlaceEvent;
import wbs.enchants.util.EnchantUtils;
import wbs.enchants.util.EventUtils;
import wbs.utils.util.persistent.WbsPersistentDataType;

import java.util.*;

import static org.jetbrains.annotations.ApiStatus.OverrideOnly;

/**
 * Represents an enchantment that can go on a block in the inventory, that will persist in the chunk data, allowing it
 * to be placed and picked up while retaining the enchantment.
 */
@SuppressWarnings("UnstableApiUsage")
public interface BlockEnchant extends EnchantInterface, AutoRegistrableEnchant {
    NamespacedKey BLOCK_ENCHANTS_KEY = WbsEnchantsBootstrap.createKey("block_enchants");
    NamespacedKey BLOCK_KEY = WbsEnchantsBootstrap.createKey("block_key");

    //region Block Keys
    static NamespacedKey getBlockKey(Block block) {
        // Don't need to track world; this tag will be stored on a chunk
        String stringKey = block.getX() + "_" + block.getY() + "_" + block.getZ();
        return new NamespacedKey(WbsEnchants.getInstance(), stringKey);
    }

    static Block getBlock(NamespacedKey key, World world) {
        String stringPosition = key.value();

        String[] components = stringPosition.split("_");

        return world.getBlockAt(Integer.parseInt(components[0]), Integer.parseInt(components[1]), Integer.parseInt(components[2]));
    }
    //endregion

    //region Block Containers
    static PersistentDataContainer getBlockContainer(Block block) {
        PersistentDataContainer chunkContainer = block.getChunk().getPersistentDataContainer();

        PersistentDataContainer enchantedBlocksContainer = chunkContainer.get(BLOCK_ENCHANTS_KEY, PersistentDataType.TAG_CONTAINER);

        if (enchantedBlocksContainer == null) {
            enchantedBlocksContainer = chunkContainer.getAdapterContext().newPersistentDataContainer();
        }

        NamespacedKey blockKey = getBlockKey(block);
        PersistentDataContainer blockContainer = enchantedBlocksContainer.get(blockKey, PersistentDataType.TAG_CONTAINER);

        if (blockContainer == null) {
            blockContainer = enchantedBlocksContainer.getAdapterContext().newPersistentDataContainer();
        }

        return blockContainer;
    }

    static void updateBlockContainer(Block block, PersistentDataContainer container) {
        PersistentDataContainer chunkContainer = block.getChunk().getPersistentDataContainer();

        PersistentDataContainer enchantedBlocksContainer = chunkContainer.get(BLOCK_ENCHANTS_KEY, PersistentDataType.TAG_CONTAINER);

        if (enchantedBlocksContainer == null) {
            enchantedBlocksContainer = chunkContainer.getAdapterContext().newPersistentDataContainer();
        }

        NamespacedKey blockKey = getBlockKey(block);

        if (container == null || container.isEmpty()) {
            enchantedBlocksContainer.remove(blockKey);
        } else {
            enchantedBlocksContainer.set(blockKey, PersistentDataType.TAG_CONTAINER, container);
        }

        if (enchantedBlocksContainer.isEmpty()) {
            chunkContainer.remove(BLOCK_ENCHANTS_KEY);
        } else {
            chunkContainer.set(BLOCK_ENCHANTS_KEY, PersistentDataType.TAG_CONTAINER, enchantedBlocksContainer);
        }
    }
    //endregion

    //region Retrieval/Querying
    static Map<Block, Map<BlockEnchant, Integer>> getBlockEnchantments(Chunk chunk) {
        PersistentDataContainer chunkContainer = chunk.getPersistentDataContainer();

        PersistentDataContainer enchantedBlocksContainer = chunkContainer.get(BLOCK_ENCHANTS_KEY, PersistentDataType.TAG_CONTAINER);

        Map<Block, Map<BlockEnchant, Integer>> enchantedBlocks = new HashMap<>();

        if (enchantedBlocksContainer == null) {
            return enchantedBlocks;
        }

        enchantedBlocksContainer.getKeys().forEach(blockKey -> {
            PersistentDataContainer blockEnchantsContainer = enchantedBlocksContainer.get(blockKey, PersistentDataType.TAG_CONTAINER);

            if (blockEnchantsContainer == null) {
                return;
            }

            Block block = getBlock(blockKey, chunk.getWorld());

            enchantedBlocks.put(block, getEnchantments(block));
        });

        return enchantedBlocks;
    }

    private boolean anyEnchanted(@NotNull List<Block> blocks) {
        return blocks.stream().anyMatch(this::isEnchanted);
    }

    private static @Nullable Integer getLevel(WbsEnchantment enchant, PersistentDataContainer blockContainer) {
        NamespacedKey key = enchant.getKey();

        return blockContainer.get(key, PersistentDataType.INTEGER);
    }

    static Map<BlockEnchant, Integer> getEnchantments(Block block) {
        PersistentDataContainer blockContainer = getBlockContainer(block);

        return getEnchantments(blockContainer);
    }

    static Map<BlockEnchant, Integer> getEnchantments(PersistentDataContainer blockContainer) {
        Map<BlockEnchant, Integer> enchantments = new HashMap<>();
        for (NamespacedKey key : blockContainer.getKeys()) {
            WbsEnchantment enchantment = EnchantManager.getCustomFromKey(key);
            if (enchantment instanceof BlockEnchant blockEnchant) {
                enchantments.put(blockEnchant, blockContainer.get(key, PersistentDataType.INTEGER));
            }
        }

        return enchantments;
    }

    static boolean hasBlockEnchants(Block block) {
        PersistentDataContainer blockContainer = getBlockContainer(block);

        return !blockContainer.isEmpty();
    }

    @Nullable
    default Integer getLevel(Block block) {
        PersistentDataContainer blockContainer = getBlockContainer(block);

        WbsEnchantment enchant = getThisEnchantment();
        return getLevel(enchant, blockContainer);
    }

    default boolean isEnchanted(Block block) {
        return getLevel(block) != null;
    }

    @Nullable
    default Integer getLevel(Inventory inventory) {
        Location location = inventory.getLocation();
        if (location == null) {
            return null;
        }

        return getLevel(location.getBlock());
    }

    default boolean isEnchanted(Inventory inventory) {
        Location location = inventory.getLocation();
        if (location == null) {
            return false;
        }

        return isEnchanted(location.getBlock());
    }
    //endregion

    //region Events

    default void registerBlockEvents() {
        EventUtils.register(BlockPistonExtendEvent.class, this::onPistonExtend, EventPriority.LOWEST, true);
        EventUtils.register(BlockPistonRetractEvent.class, this::onPistonRetract, EventPriority.LOWEST, true);

        EventUtils.register(BlockPlaceEvent.class, this::onPlace, EventPriority.NORMAL, true);
        EventUtils.register(BlockDropItemEvent.class, this::onDrop, EventPriority.NORMAL, true);

        EventUtils.register(BlockBreakBlockEvent.class, this::onBlockBreakBlock, EventPriority.HIGHEST, true);
        EventUtils.register(BlockExplodeEvent.class, this::onExplode, EventPriority.HIGHEST, true);
        EventUtils.register(EntityExplodeEvent.class, this::onExplode, EventPriority.HIGHEST, true);

        EventUtils.register(EntityChangeBlockEvent.class, this::onEnchantedBlockFall, EventPriority.MONITOR, true);
        EventUtils.register(AnvilDamagedEvent.class, this::onAnvilDamage, EventPriority.MONITOR, true);
        EventUtils.register(SpongeAbsorbEvent.class, this::onSpongeChange, EventPriority.MONITOR, true);
    }

    // TODO: Add piston movement functionality
    default void onPistonExtend(BlockPistonExtendEvent event) {
        if (anyEnchanted(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    default void onPistonRetract(BlockPistonRetractEvent event) {
        if (anyEnchanted(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    private void onBlockBreakBlock(BlockBreakBlockEvent event) {
        Block brokenBlock = event.getBlock();

        Integer level = removeEnchant(brokenBlock);
        if (level != null) {
            afterRemoveEnchant(brokenBlock.getState(), event.getDrops(), brokenBlock, getThisEnchantment(), level);
        }
    }

    // Called from SharedEventsHandler
    default void onLoad(ChunkLoadEvent event, Block block, int level) {
        if (!canEnchant(block)) {
            removeEnchant(block);
        } else {
            removeEnchantmentGlints(block);
            createEnchantmentGlint(block, (Material) null);
        }

        afterLoad(event, block, level);
    }

    private void onSpongeChange(SpongeAbsorbEvent event) {
        Block block = event.getBlock();
        Integer level = getLevel(block);
        if (level != null) {
            removeEnchantmentGlints(block);
            createEnchantmentGlint(block, Material.WET_SPONGE);
        }
    }

    private void onEnchantedBlockFall(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock entity) {
            @NotNull Block block = event.getBlock();
            WbsEnchantment enchant = getThisEnchantment();
            if (event.getTo().isAir()) { // Turn into entity
                if (isEnchanted(block)) {
                    Integer level = removeEnchant(block, enchant);
                    if (level != null) {
                        entity.getPersistentDataContainer().set(enchant.getKey(), PersistentDataType.INTEGER, level);
                    }
                }
            } else { // Turn into block
                Integer level = entity.getPersistentDataContainer().get(enchant.getKey(), PersistentDataType.INTEGER);
                if (level != null) {
                    enchantBlock(block, enchant, level, entity.getBlockData());
                }
            }
        }
    }

    private void onAnvilDamage(AnvilDamagedEvent event) {
        Location location = event.getInventory().getLocation();
        if (location == null) {
            return;
        }

        Block block = location.getBlock();
        Integer level = getLevel(block);
        if (level != null) {
            if (event.isBreaking()) {
                removeEnchant(block);
            } else {
                Material newMaterial = event.getDamageState().getMaterial();
                removeEnchantmentGlints(block);
                createEnchantmentGlint(block, newMaterial);
            }
        }
    }

    private void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();

        Block block = event.getBlock();
        if (!canEnchant(block)) {
            return;
        }

        WbsEnchantment enchant = getThisEnchantment();

        ItemStack placedItem = equipment.getItem(event.getHand());
        if (enchant.isEnchantmentOn(placedItem)) {
            int level = enchant.getLevel(placedItem);

            enchantBlock(block, enchant, level);

            afterPlace(event, placedItem);
        }
    }

    private void onExplode(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();

        handleExplode(event.getBlock().getLocation().toCenterLocation(), event.getExplosionResult(), blocks, null);
    }

    private void onExplode(EntityExplodeEvent event) {
        List<Block> blocks = event.blockList();

        handleExplode(event.getLocation(), event.getExplosionResult(), blocks, event.getEntity());
    }

    private void handleExplode(@NotNull Location location, @NotNull ExplosionResult result, List<Block> blocks, @Nullable Entity entity) {
        if (result != ExplosionResult.DESTROY && result != ExplosionResult.DESTROY_WITH_DECAY) {
            return;
        }

        OptionalDouble optionalMax = blocks.stream()
                .mapToDouble(block -> block.getLocation().distanceSquared(location))
                .max();

        if (optionalMax.isEmpty()) {
            // No blocks
            return;
        }

        float radius = (float) Math.sqrt(optionalMax.getAsDouble());

        List<Block> enchantedBlocks = new LinkedList<>();
        WbsEnchantment enchant = getThisEnchantment();

        World world = location.getWorld();

        for (Block block : blocks) {
            Integer level = removeEnchant(block, enchant);

            if (level != null) {
                ServerLevel serverLevel = ((CraftWorld) world).getHandle();
                BlockState state = block.getState();
                net.minecraft.world.level.block.state.BlockState nmsBlockState = ((CraftBlockState) state).getHandle();

                Map<ItemStack, Location> drops = new HashMap<>();

                nmsBlockState.onExplosionHit(
                        serverLevel,
                        ((CraftBlock) block).getPosition(),
                        buildExplosion(serverLevel, entity, location, radius, result),
                        (item, pos) -> {
                            drops.put(CraftItemStack.asBukkitCopy(item), CraftLocation.toBukkit(pos));
                        }
                );

                afterRemoveEnchant(state, drops.keySet(), block, enchant, level);
                drops.forEach((item, itemLoc) -> {
                    world.dropItem(location, item);
                });

                enchantedBlocks.add(block);
            }
        }

        blocks.removeAll(enchantedBlocks);
    }

    private ServerExplosion buildExplosion(ServerLevel level, @Nullable Entity entity, Location location, float radius, ExplosionResult result) {
        Explosion.BlockInteraction interaction = switch (result) {
            case KEEP -> Explosion.BlockInteraction.KEEP;
            case DESTROY -> Explosion.BlockInteraction.DESTROY;
            case DESTROY_WITH_DECAY -> Explosion.BlockInteraction.DESTROY_WITH_DECAY;
            case TRIGGER_BLOCK -> Explosion.BlockInteraction.TRIGGER_BLOCK;
        };

        net.minecraft.world.entity.Entity entitySource = null;
        if (entity instanceof CraftEntity craftEntity) {
            entitySource = craftEntity.getHandle();
        }

        return new ServerExplosion(
                level,
                entitySource,
                Explosion.getDefaultDamageSource(level, entitySource),
                null,
                CraftLocation.toVec3(location),
                radius,
                false, // Don't create fire; no effect on loot anyway
                interaction
        );
    }

    private void onDrop(BlockDropItemEvent event) {
        WbsEnchantment enchant = getThisEnchantment();

        Block block = event.getBlock();
        Integer level = removeEnchant(block, enchant);
        if (level != null) {
            afterRemoveEnchant(event.getBlockState(), event.getItems().stream().map(Item::getItemStack).toList(), block, enchant, level);
        }
    }

    private void afterRemoveEnchant(@NotNull BlockState blockState, Collection<ItemStack> items, Block block, WbsEnchantment enchant, Integer level) {
        EnchantedBlockBreakEvent enchantedEvent = new EnchantedBlockBreakEvent(this, block);
        Bukkit.getPluginManager().callEvent(enchantedEvent);

        ItemStack dropped = null;
        Material material = blockState.getBlockData().getPlacementMaterial();
        for (ItemStack item : items) {
            if (item.getType() == material && item.getAmount() == 1) {
                dropped = item;
                break;
            }
        }

        if (dropped == null) {
            return;
        }

        EnchantUtils.addEnchantment(enchant, dropped, level);

        afterDrop(blockState, dropped);
    }

    //endregion

    //region Enchanting/Unenchanting

    private void enchantBlock(Block block, WbsEnchantment enchant, int level) {
        enchantBlock(block, enchant, level, block.getBlockData());
    }

    Set<Material> UNSUPPORTED_GLINT = Set.of(
            // Item only forms
            Material.HOPPER,
            Material.BELL,
            Material.BREWING_STAND,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE
    );

    Set<TagKey<ItemType>> UNSUPPORTED_GLINT_TAG = Set.of(
            // Item only forms
            ItemTypeTagKeys.SHULKER_BOXES,
            ItemTypeTagKeys.BEDS,
            ItemTypeTagKeys.DOORS,
            ItemTypeTagKeys.SIGNS,
            ItemTypeTagKeys.HANGING_SIGNS,
            ItemTypeTagKeys.CANDLES,
            ItemTypeTagKeys.FLOWERS,
            // Stateful
            ItemTypeTagKeys.FENCE_GATES,
            ItemTypeTagKeys.TRAPDOORS // TODO: Put functionality?
            // Maybe pressure plates? barely changes tho
    );

    private void enchantBlock(Block block, WbsEnchantment enchant, int level, @NotNull BlockData renderAs) {
        PersistentDataContainer blockContainer = getBlockContainer(block);

        blockContainer.set(enchant.getKey(), PersistentDataType.INTEGER, level);

        updateBlockContainer(block, blockContainer);

        createEnchantmentGlint(block, renderAs);

        EnchantedBlockPlaceEvent enchantedEvent = new EnchantedBlockPlaceEvent((BlockEnchant) enchant, block);
        Bukkit.getPluginManager().callEvent(enchantedEvent);
    }

    private @Nullable Integer removeEnchant(Block block) {
        return removeEnchant(block, getThisEnchantment());
    }
    private static @Nullable Integer removeEnchant(Block block, WbsEnchantment enchant) {
        return removeEnchant(block, enchant.getKey());
    }
    private static @Nullable Integer removeEnchant(Block block, NamespacedKey key) {
        PersistentDataContainer blockContainer = getBlockContainer(block);

        Integer level = blockContainer.get(key, PersistentDataType.INTEGER);

        blockContainer.remove(key);
        if (level != null) {
            removeEnchantmentGlints(block);
        }
        updateBlockContainer(block, blockContainer);
        return level;
    }

    //endregion

    //region Enchantment Glints

    static void createEnchantmentGlint(Block block, @Nullable Material renderAs) {
        BlockData realData = block.getBlockData();
        BlockData data = renderAs == null ? realData : renderAs.createBlockData().merge(realData);
        createEnchantmentGlint(block, data);
    }
    static void createEnchantmentGlint(Block block, @NotNull BlockData renderAs) {
        if (!isSupported(renderAs)) {
            return;
        }

        BlockFace face = getFace(renderAs);
        Vector direction = face.getDirection();

        block.getWorld().spawn(block.getLocation().toCenterLocation().setDirection(direction), ItemDisplay.class, CreatureSpawnEvent.SpawnReason.ENCHANTMENT, display -> {
            ItemStack itemStack = new ItemStack(renderAs.getMaterial());
            itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
            display.setItemStack(itemStack);
            display.setBrightness(new Display.Brightness(15, 15));

            Transformation transformation = getTransformation(renderAs);
            display.setTransformation(transformation);
            display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            display.getPersistentDataContainer().set(BLOCK_KEY, WbsPersistentDataType.NAMESPACED_KEY, getBlockKey(block));
        });
    }

    Set<Material> KEEP_FACE_VISIBLE = Set.of(
            Material.FURNACE,
            Material.SMOKER,
            Material.BLAST_FURNACE,
            Material.CHISELED_BOOKSHELF
    );

    private static @NotNull Transformation getTransformation(@NotNull BlockData renderAs) {
        float expansion = 0.001f;
        float base = 1f;
        float overlayScale = base + expansion;
        Vector3f scale = new Vector3f(overlayScale, overlayScale, overlayScale);
        Vector3f translation = new Vector3f();

        if (KEEP_FACE_VISIBLE.contains(renderAs.getMaterial())) {
            // Always use Z -- rotation is using location, not transformation.
            scale = new Vector3f(overlayScale, overlayScale, overlayScale - (expansion * 2));
            translation = new Vector3f(0, 0, -expansion * 2);
        }

        return new Transformation(
                translation,
                new AxisAngle4f(),
                scale,
                new AxisAngle4f()
        );
    }

    private static boolean isSupported(@NotNull BlockData renderAs) {
        if (UNSUPPORTED_GLINT.contains(renderAs.getMaterial())) {
            return false;
        }

        ItemType itemType = renderAs.getMaterial().asItemType();
        if (itemType == null) {
            return false;
        }

        TypedKey<ItemType> itemTypeKey = RegistryKey.ITEM.typedKey(itemType.key());
        Registry<@NotNull ItemType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM);
        boolean unsupported = UNSUPPORTED_GLINT_TAG.stream()
                .anyMatch(key -> registry.getTag(key).contains(itemTypeKey));

        return !unsupported;
    }

    private static @NotNull BlockFace getFace(BlockData block) {
        BlockFace face = BlockFace.NORTH;

        if (block instanceof Directional directional) {
            face = directional.getFacing();
        }

        if (block instanceof Orientable orientable) {
            face = switch (orientable.getAxis()) {
                case X -> BlockFace.EAST;
                case Y -> BlockFace.UP;
                case Z -> BlockFace.SOUTH;
            };
        }

        return face;
    }

    static void removeEnchantmentGlints(Block block) {
        NamespacedKey blockKey = getBlockKey(block);
        block.getWorld().getNearbyEntitiesByType(ItemDisplay.class, block.getLocation(), 2, display -> {
            NamespacedKey entityBlockKey = display.getPersistentDataContainer().get(BLOCK_KEY, WbsPersistentDataType.NAMESPACED_KEY);
            return blockKey.equals(entityBlockKey);
        }).forEach(Entity::remove);
    }

    //endregion

    //region Overridable Methods

    @OverrideOnly
    default void afterPlace(BlockPlaceEvent event, ItemStack placedItem) {

    }

    @OverrideOnly
    default void afterDrop(@NotNull org.bukkit.block.BlockState state, ItemStack droppedItem) {

    }

    @OverrideOnly
    default void afterLoad(ChunkLoadEvent event, Block block, int level) {

    }

    //endregion

    boolean canEnchant(Block block);
}
