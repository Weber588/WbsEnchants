package wbs.enchants.enchantment.curse;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.ConflictEnchantment;

public class CurseVanilla extends WbsEnchantment implements ConflictEnchantment {
    public CurseVanilla() {
        super("curse_vanilla");
    }

    @Override
    public @NotNull String getDescription() {
        return "A curse that does nothing alone, but is incompatible with all non-vanilla enchants!";
    }

    @Override
    public String getDisplayName() {
        return "&cCurse of Vanilla";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        //noinspection deprecation
        return EnchantmentTarget.ALL;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return !enchantment.getKey().getNamespace().equals("minecraft");
    }

    @Override
    public String getConflictsDescription() {
        return "All custom enchantments";
    }
}
