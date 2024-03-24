package wbs.enchants.util;

import com.destroystokyo.paper.MaterialSetTag;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.papermc.paper.tag.EntitySetTag;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchants;

import java.util.*;

public class TagUtils {
    private static final Multimap<String, Tag<?>> ALL_TAGS = HashMultimap.create();

    public static final String REGISTRY_MATERIALS = "materials";

    private static final TagGetter<Material> MATERIAL_GETTER = key -> {
        boolean isTag = false;
        if (key.startsWith("#")) {
            isTag = true;
            key = key.substring(1);
        }

        NamespacedKey entryKey;
        if (key.contains(":")) {
            String[] components = key.split(":");
            entryKey = new NamespacedKey(components[0], components[1]);
        } else {
            entryKey = NamespacedKey.minecraft(key);
        }

        if (isTag) {
            Tag<Material> found = tryGetTag(Tag.REGISTRY_BLOCKS, entryKey, Material.class);

            if (found == null) {
                found = tryGetTag(Tag.REGISTRY_ITEMS, entryKey, Material.class);
            }

            if (found != null) {
                return found.getValues();
            }
            // Might be worth erroring here, but I think it's fine to just check for (say) "minecraft:iron_ingot" if
            // the user accidentally typed it as "#minecraft:iron_ingot"
        }

        for (Material check : Material.values()) {
            if (check.getKey().equals(entryKey)) {
                return Set.of(check);
            }
        }

        return Collections.emptySet();
    };

    public static Tag<Material> WOODEN_TOOLS = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "tools/wooden");
    public static Tag<Material> STONE_TOOLS = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "tools/stone");
    public static Tag<Material> IRON_TOOLS = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "tools/iron");
    public static Tag<Material> GOLDEN_TOOLS = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "tools/golden");
    public static Tag<Material> DIAMOND_TOOLS = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "tools/diamond");
    public static Tag<Material> NETHERITE_TOOLS = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "tools/netherite");

    public static Tag<Material> LEATHER_ARMOUR = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "armour/leather");
    public static Tag<Material> CHAINMAIL_ARMOUR = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "armour/chainmail");
    public static Tag<Material> IRON_ARMOUR = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "armour/iron");
    public static Tag<Material> GOLDEN_ARMOUR = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "armour/golden");
    public static Tag<Material> DIAMOND_ARMOUR = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "armour/diamond");
    public static Tag<Material> NETHERITE_ARMOUR = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "armour/netherite");
    public static Tag<Material> TURTLE_ARMOUR = registerTag(REGISTRY_MATERIALS, Material.class, MATERIAL_GETTER, "armour/netherite");

    private static <T extends Keyed> Tag<T> registerTag(String registryName, Class<T> clazz, TagGetter<T> getter, String stringKey) {
        NamespacedKey key = new NamespacedKey(WbsEnchants.getInstance(), stringKey);

        List<String> stringEntries = new LinkedList<>(); // TODO: Make this read from tags.yml instead!!!

        Set<T> entries = new HashSet<>();
        for (String entry : stringEntries) {
            entries.addAll(getter.from(entry));
        }

        Tag<T> tag = toTag(clazz, key, entries);
        ALL_TAGS.put(registryName, tag);
        return tag;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Keyed> Tag<T> toTag(Class<T> clazz, NamespacedKey key, Set<T> entries) {
        if (clazz == Material.class) {
            return (Tag<T>) new MaterialSetTag(key, (Set<Material>) entries);
        } else if (clazz == EntityType.class) {
            return (Tag<T>) new EntitySetTag(key, (Set<EntityType>) entries);
        } else {
            throw new NotImplementedException("Unsupported tag type: " + clazz.getCanonicalName());
        }
    }

    @Nullable
    public static <T extends Keyed> Tag<T> tryGetTag(String registryName, NamespacedKey tagKey, Class<T> clazz) {
        Collection<Tag<?>> registry = ALL_TAGS.get(registryName);

        for (Tag<?> tag : registry) {
            if (!tag.getKey().equals(tagKey)) {
                continue;
            }

            Keyed instanceOfTag = tag.getValues().stream().findAny().orElse(null);
            if (instanceOfTag != null) {
                if (clazz.isAssignableFrom(instanceOfTag.getClass())) {
                    //noinspection unchecked
                    return (Tag<T>) tag;
                }
            }
        }

        return Bukkit.getTag(registryName, tagKey, clazz);
    }

    @FunctionalInterface
    private interface TagGetter<T> {
        Collection<T> from(String stringKey);
    }
}
