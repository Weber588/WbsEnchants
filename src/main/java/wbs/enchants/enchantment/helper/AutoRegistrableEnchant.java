package wbs.enchants.enchantment.helper;

public interface AutoRegistrableEnchant {
    default boolean autoRegister() {
        return true;
    }
}
