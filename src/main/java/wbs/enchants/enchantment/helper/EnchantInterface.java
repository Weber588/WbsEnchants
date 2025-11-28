package wbs.enchants.enchantment.helper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

public interface EnchantInterface {
    @NotNull
    @Contract("-> this")
    default WbsEnchantment getThisEnchantment() {
        if (this instanceof WbsEnchantment enchantment) {
            return enchantment;
        }
        throw new IllegalStateException("Implementors of EnchantInterface must extend WbsEnchantment");
    }
}
