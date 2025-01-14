package wbs.enchants.definition;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.util.EnchantUtils;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class EnchantmentWrapper implements Keyed {
    @NotNull
    private final Key key;

    public EnchantmentWrapper(@NotNull Key key) {
        this.key = key;
    }

    @NotNull
    public Key key() {
        return key;
    }

    @NotNull
    public Enchantment getEnchantment() {
        Enchantment enchantment = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(this.key());

        if (enchantment == null) {
            throw new IllegalStateException("Server enchantment not found for enchantment \"" + this.key() + "\".");
        }

        return enchantment;
    }

    public boolean isEnchantmentOn(@NotNull ItemStack item) {
        return item.containsEnchantment(getEnchantment());
    }

    public boolean tryAdd(ItemStack stack, int level) {
        Enchantment enchantment = getEnchantment();
        if (stack.getType() != Material.ENCHANTED_BOOK && !enchantment.canEnchantItem(stack)) {
            return false;
        }

        Set<Enchantment> existing = new HashSet<>();
        if (stack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            existing = meta.getStoredEnchants().keySet();
        } else {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                existing = meta.getEnchants().keySet();
            }
        }

        for (Enchantment other : existing) {
            if (enchantment.conflictsWith(other)) {
                return false;
            }
        }

        EnchantUtils.addEnchantment(this, stack, level);

        return true;
    }

    public TypedKey<Enchantment> getTypedKey() {
        return TypedKey.create(RegistryKey.ENCHANTMENT, key());
    }

    public boolean isTagged(TagKey<Enchantment> tagKey) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.getTag(tagKey).contains(this.getTypedKey());
    }
}
