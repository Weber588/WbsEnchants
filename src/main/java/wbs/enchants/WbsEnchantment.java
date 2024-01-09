package wbs.enchants;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsEnums;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class WbsEnchantment extends UberEnchantment {
    private final String stringKey;

    public WbsEnchantment(String key) {
        super(new NamespacedKey(WbsEnchants.getInstance(), key));
        stringKey = key;
        EnchantsSettings.register(this);
    }

    @NotNull
    public abstract String getDescription();
    @NotNull
    public String getTargetDescription() {
        return switch (getItemTarget()) {
            case ARMOR_HEAD -> "Helmet";
            case ARMOR_TORSO -> "Chestplate";
            case ARMOR_LEGS -> "Leggings";
            case ARMOR_FEET -> "Boots";
            default -> WbsEnums.toPrettyString(getItemTarget());
        };
    }

    @Override
    public String getPermission() {
        return "wbsenchants.enchantment." + getName();
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    public List<String> getAliases() {
        return new LinkedList<>();
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return getItemTarget().includes(itemStack);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return new NamespacedKey(WbsEnchants.getInstance(), stringKey);
    }

    @NotNull
    protected Map<NamespacedKey, Double> getLootKeyChances() {
        return new HashMap<>();
    }

    @Nullable
    public Double getAddToChance(LootTable table) {
        return getLootKeyChances().get(table.getKey());
    }

    public boolean matches(String asString) {
        if (stringKey.equalsIgnoreCase(asString)) {
            return true;
        }

        return getAliases().stream().anyMatch(alias -> alias.equalsIgnoreCase(asString));
    }

    public boolean looselyMatches(String asString) {
        asString = asString.toLowerCase();
        if (stringKey.startsWith(asString)) {
            return true;
        }

        String finalAsString = asString;
        return getAliases().stream().anyMatch(alias -> alias.startsWith(finalAsString));
    }
}
