package wbs.enchants.type;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

import java.util.*;
import java.util.function.Predicate;

public class EnchantmentTypeManager {
    private static final Map<Key, EnchantmentType> REGISTERED_TYPES = new HashMap<>();

    public static final RegularEnchantmentType REGULAR = new RegularEnchantmentType();
    public static final CurseEnchantmentType CURSE = new CurseEnchantmentType();
    public static final ParadoxicalEnchantmentType PARADOXICAL = new ParadoxicalEnchantmentType();
    public static final EtherealEnchantmentType ETHEREAL = new EtherealEnchantmentType();

    public static EnchantmentType register(EnchantmentType type) {
        if (REGISTERED_TYPES.containsKey(type.getKey())) {
            throw new IllegalStateException("Type already registered: " + type.getKey());
        }
        REGISTERED_TYPES.put(type.getKey(), type);
        return type;
    }

    public static EnchantmentType getType(Enchantment enchantment) {
        for (EnchantmentType type : getRegistered()) {
            if (getEnchantmentsOfType(type).contains(enchantment)) {
                return type;
            }
        }

        return REGULAR;
    }

    public static List<Enchantment> getEnchantmentsOfType(EnchantmentType type) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

        TagKey<Enchantment> tagKey = type.getTagKey();
        if (tagKey != null) {
            return getEnchantmentsInTag(registry, tagKey);
        }

        // No tag key was identified -- get all enchants not in an enchantment type key (REGULAR)
        List<Enchantment> typeTagged = new LinkedList<>();

        getRegistered().forEach((otherType) -> {
            typeTagged.addAll(getEnchantmentsInTag(registry, otherType.getTagKey()));
        });

        return registry.stream().filter(Predicate.not(typeTagged::contains)).toList();
    }

    private static List<Enchantment> getEnchantmentsInTag(Registry<Enchantment> registry, TagKey<Enchantment> tagKey) {
        if (tagKey == null) {
            return new LinkedList<>();
        }

        Collection<TypedKey<Enchantment>> enchantmentKeys = registry
                .getTag(tagKey)
                .values();

        return registry.stream().filter(enchantment -> {
            TypedKey<Enchantment> typedKey = TypedKey.create(RegistryKey.ENCHANTMENT, enchantment.key());
            return enchantmentKeys.contains(typedKey);
        }).toList();
    }

    public static Collection<EnchantmentType> getRegistered() {
        return Collections.unmodifiableCollection(REGISTERED_TYPES.values());
    }
}
