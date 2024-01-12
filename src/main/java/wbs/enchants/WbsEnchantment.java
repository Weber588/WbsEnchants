package wbs.enchants;

import me.sciguymjm.uberenchant.api.UberEnchantment;
import me.sciguymjm.uberenchant.api.utils.UberUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;

import java.util.*;

public abstract class WbsEnchantment extends UberEnchantment {
    private static final Random RANDOM = new Random();

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

    public void onLootGenerate(LootGenerateEvent event) {
        Double chance = getAddToChance(event.getLootTable());

        if (chance == null) {
            return;
        }

        if (!WbsMath.chance(chance)) {
            return;
        }

        for (ItemStack stack : event.getLoot()) {
            int level;
            int maxLevel = getMaxLevel();
            if (maxLevel < 1) {
                level = 0;
            } else {
                level = RANDOM.nextInt(maxLevel) + 1;
            }
            if (tryAdd(stack, level)) {
                return;
            }
        }
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

    protected boolean tryAdd(ItemStack stack, int level) {
        if (stack.getType() != Material.ENCHANTED_BOOK && !canEnchantItem(stack)) {
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
            if (conflictsWith(other)) {
                return false;
            }
        }

        if (stack.getType() == Material.ENCHANTED_BOOK) {
            UberUtils.addStoredEnchantment(this, stack, level);
        } else {
            UberUtils.addEnchantment(this, stack, level);
        }

        return true;
    }
}
