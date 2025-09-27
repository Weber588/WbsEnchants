package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.MapDecorations;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.TickableEnchant;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class AnnotatedEnchant extends WbsEnchantment implements TickableEnchant {
    private static final NamespacedKey LAST_UPDATED = WbsEnchantsBootstrap.createKey("last_updated_annotated");


    private static final HashMap<NamespacedKey, MapCursor.Type> STRUCTURE_CURSORS = new HashMap<>();
    private static final HashMap<String, MapCursor.Type> STRUCTURE_CURSOR_GUESSES = new HashMap<>();
    public static final MapCursor.Type DEFAULT_CURSOR = MapCursor.Type.PLAYER_OFF_LIMITS;


    static {
        // TODO: Make this configurable

        // Direct
        STRUCTURE_CURSORS.put(StructureType.BURIED_TREASURE.getKey(), MapCursor.Type.RED_X);
        STRUCTURE_CURSORS.put(StructureType.WOODLAND_MANSION.getKey(), MapCursor.Type.MANSION);
        STRUCTURE_CURSORS.put(StructureType.OCEAN_MONUMENT.getKey(), MapCursor.Type.MONUMENT);
        STRUCTURE_CURSORS.put(StructureType.SWAMP_HUT.getKey(), MapCursor.Type.SWAMP_HUT);
        STRUCTURE_CURSORS.put(StructureType.JUNGLE_TEMPLE.getKey(), MapCursor.Type.JUNGLE_TEMPLE);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("trial_chamber"), MapCursor.Type.TRIAL_CHAMBERS);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("village_desert"), MapCursor.Type.VILLAGE_DESERT);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("village_plains"), MapCursor.Type.VILLAGE_PLAINS);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("village_savanna"), MapCursor.Type.VILLAGE_SAVANNA);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("village_snowy"), MapCursor.Type.VILLAGE_SNOWY);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("village_taiga"), MapCursor.Type.VILLAGE_TAIGA);

        // Adaptations
        // Above ground
        STRUCTURE_CURSORS.put(StructureType.DESERT_PYRAMID.getKey(), MapCursor.Type.BANNER_YELLOW);
        STRUCTURE_CURSORS.put(StructureType.END_CITY.getKey(), MapCursor.Type.BANNER_PURPLE);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("pillager_outpost"), MapCursor.Type.BANNER_RED);
        // Below ground
        STRUCTURE_CURSORS.put(StructureType.MINESHAFT.getKey(), MapCursor.Type.RED_X);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("mineshaft_mesa"), MapCursor.Type.RED_X);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("ancient_city"), MapCursor.Type.RED_X);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("trail_ruins"), MapCursor.Type.RED_X);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("stronghold"), MapCursor.Type.RED_X);
        // Ocean
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("ocean_ruin_cold"), MapCursor.Type.BANNER_BLUE);
        STRUCTURE_CURSORS.put(NamespacedKey.minecraft("ocean_ruin_warm"), MapCursor.Type.BANNER_LIGHT_BLUE);


        STRUCTURE_CURSOR_GUESSES.put("village", MapCursor.Type.VILLAGE_PLAINS);
        STRUCTURE_CURSOR_GUESSES.put("mineshaft", MapCursor.Type.RED_X);
        STRUCTURE_CURSOR_GUESSES.put("underground", MapCursor.Type.RED_X);
        STRUCTURE_CURSOR_GUESSES.put("dungeon", MapCursor.Type.RED_X);
        STRUCTURE_CURSOR_GUESSES.put("portal", MapCursor.Type.BANNER_PURPLE);
        STRUCTURE_CURSOR_GUESSES.put("ocean", MapCursor.Type.BANNER_LIGHT_BLUE);
        STRUCTURE_CURSOR_GUESSES.put("water", MapCursor.Type.BANNER_LIGHT_BLUE);
        STRUCTURE_CURSOR_GUESSES.put("fortress", MapCursor.Type.BANNER_RED);
        STRUCTURE_CURSOR_GUESSES.put("bastion", MapCursor.Type.BANNER_BLACK);
    }

    private static final long UPDATE_FREQUENCY = Ticks.SINGLE_TICK_DURATION_MS * Ticks.TICKS_PER_SECOND * 60;
    private static final String DEFAULT_DESCRIPTION = "Maps with this enchantment will show many structures on the map.";

    public AnnotatedEnchant() {
        super("annotated", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.MAPS)
                .exclusiveWith(WbsEnchantsBootstrap.EXCLUSIVE_SET_MAPS)
                .addInjectInto(WbsEnchantsBootstrap.EXCLUSIVE_SET_MAPS)
                .activeSlots(EquipmentSlotGroup.HAND)
                .weight(5)
                .targetDescription("Map")
                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(3, 6))
                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(9, 6));
    }

    @Override
    public int getTickFrequency() {
        return 20;
    }

    @EventHandler
    public void onLoadChunk(PlayerChunkLoadEvent event) {
        Player player = event.getPlayer();

        ItemStack mainItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        boolean isMainItemEnchanted = isEnchantmentOn(mainItem);
        boolean isOffHandItemEnchanted = isEnchantmentOn(offHandItem);
        if (!isMainItemEnchanted && !isOffHandItemEnchanted) {
            return;
        }

        Chunk chunk = event.getChunk();

        Map<String, MapDecorations.DecorationEntry> decorationEntryMap = getDecorations(chunk.getStructures(), chunk.getWorld());

        if (!decorationEntryMap.isEmpty()) {
            if (isMainItemEnchanted) {
                updateItem(mainItem, decorationEntryMap, false);
            }
            if (isOffHandItemEnchanted) {
                updateItem(offHandItem, decorationEntryMap, false);
            }
        }

    }

    @Override
    public void onTickEquipped(LivingEntity owner, ItemStack itemStack, EquipmentSlot slot) {
        if (!getActiveSlots().contains(slot)) {
            return;
        }

        if (itemStack.getType() != Material.FILLED_MAP) {
            return;
        }

        Long lastUpdated = itemStack.getPersistentDataContainer().get(LAST_UPDATED, PersistentDataType.LONG);

        if (lastUpdated == null || lastUpdated < System.currentTimeMillis() - UPDATE_FREQUENCY) {
            if (itemStack.getItemMeta() instanceof MapMeta mapMeta) {
                if (!mapMeta.hasMapView()) {
                    return;
                }

                MapView map = Objects.requireNonNull(mapMeta.getMapView());

                World world = map.getWorld();
                if (world == null) {
                    throw new IllegalStateException("World cannot be null in map initialize event");
                }

                itemStack.editPersistentDataContainer(container ->
                        container.set(LAST_UPDATED, PersistentDataType.LONG, System.currentTimeMillis())
                );

                int widthMultiplier = switch (map.getScale()) {
                    case CLOSEST -> 1;
                    case CLOSE -> 2;
                    case NORMAL -> 4;
                    case FAR -> 8;
                    case FARTHEST -> 16;
                };

                int halfWorldWidth = widthMultiplier * 64;

                Set<CompletableFuture<Chunk>> futures = new HashSet<>();
                for (int x = map.getCenterX() - halfWorldWidth; x < map.getCenterX() + halfWorldWidth; x+= 16) {
                    for (int z = map.getCenterZ() - halfWorldWidth; z < map.getCenterZ() + halfWorldWidth; z += 16) {
                        Location loc = new Location(world, x, 0, z);
                        if (world.isPositionLoaded(loc)) {
                            futures.add(world.getChunkAtAsync(loc, false));
                        }
                    }
                }

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                    Set<Chunk> chunks = new HashSet<>();
                    for (CompletableFuture<Chunk> future : futures) {
                        Chunk chunk = future.join();
                        if (chunk != null && chunk.isGenerated()) {
                            chunks.add(chunk);
                        }
                    }

                    WbsEnchants.getInstance().getAsync(() -> {
                        Set<GeneratedStructure> structures = new HashSet<>();

                        for (Chunk chunk : chunks) {
                            structures.addAll(chunk.getStructures());
                        }

                        return structures;
                    }, structures -> {
                        Map<String, MapDecorations.DecorationEntry> decorationEntryMap = getDecorations(structures, world);

                        if (!decorationEntryMap.isEmpty()) {
                            Player player = Bukkit.getPlayer(owner.getUniqueId());
                            if (player != null && player.isOnline()) {
                                player.getInventory().forEach(updatedItem -> {
                                    if (updatedItem != null && updatedItem.isSimilar(itemStack)) {
                                        updateItem(updatedItem, decorationEntryMap, true);
                                    }
                                });
                            }
                        }
                    });
                });
            }
        }
    }

    private static void updateItem(ItemStack toUpdate, Map<String, MapDecorations.DecorationEntry> decorationEntryMap, boolean forceLastUpdated) {
        MapDecorations.Builder decorationsBuilder = MapDecorations.mapDecorations();

        MapDecorations existingDecorations = toUpdate.getData(DataComponentTypes.MAP_DECORATIONS);
        if (existingDecorations != null) {
            decorationsBuilder.putAll(existingDecorations.decorations());
        }

        decorationsBuilder.putAll(decorationEntryMap);

        MapDecorations finalDecorations = decorationsBuilder.build();

        if (!forceLastUpdated && existingDecorations != null && finalDecorations.decorations().keySet().equals(existingDecorations.decorations().keySet())) {
            // No changes -- don't bother updating
            return;
        }

        toUpdate.setData(
                DataComponentTypes.MAP_DECORATIONS,
                finalDecorations
        );
        toUpdate.editPersistentDataContainer(container ->
                container.set(LAST_UPDATED, PersistentDataType.LONG, System.currentTimeMillis())
        );
    }

    private Map<String, MapDecorations.DecorationEntry> getDecorations(Collection<GeneratedStructure> structures, World world) {
        Map<String, MapDecorations.DecorationEntry> decorationEntryMap = new HashMap<>();

        Registry<Structure> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE);
        for (GeneratedStructure structure : structures) {
            Location location = structure.getBoundingBox().getCenter().toLocation(world);

            // If the highest point in the world at the center of the structure is, itself, inside the structure,
            // it's probably on the surface. If not, it's probably underground.
            // Features might be able to ruin this, but that's probably okay?
            int highestSurfaceBlock = world.getHighestBlockYAt(location, HeightMap.WORLD_SURFACE_WG);
            if (structure.getBoundingBox().getMaxY() < highestSurfaceBlock) {
                continue;
            }

            NamespacedKey key = registry.getKey(structure.getStructure());
            if (key == null) {
                continue;
            }

            MapCursor.Type cursorType = STRUCTURE_CURSORS.get(key);
            if (cursorType == null) {
                for (String substring : STRUCTURE_CURSOR_GUESSES.keySet()) {
                    if (key.asMinimalString().contains(substring)) {
                        cursorType = STRUCTURE_CURSOR_GUESSES.get(substring);
                        break;
                    }
                }
            }

            // TODO: Make default type configurable
            if (cursorType == null) {
                cursorType = DEFAULT_CURSOR;
            }

            MapDecorations.DecorationEntry decorationEntry = MapDecorations.decorationEntry(
                    cursorType,
                    location.blockX(),
                    location.blockZ(),
                    cursorType.key().value().contains("banner") ? 0 : 180
            );
            decorationEntryMap.put(key.value() + location.blockX() + location.blockZ(), decorationEntry);
        }

        return decorationEntryMap;
    }
}
