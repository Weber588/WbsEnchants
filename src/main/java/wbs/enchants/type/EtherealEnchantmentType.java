package wbs.enchants.type;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.util.EventUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EtherealEnchantmentType extends EnchantmentType {
    EtherealEnchantmentType() {
        super("Ethereal");
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.text("A powerful enchantment that is removed from your item on death.");
    }

    @Override
    public TextColor getColour() {
        return TextColor.color(0xffdd99);
    }

    @Override
    public void registerListeners() {
        EventUtils.register(PlayerDeathEvent.class, this::onDeath);
    }

    private void onDeath(PlayerDeathEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();

        for (ItemStack itemStack : inventory) {
            if (itemStack != null) {
                Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();

                Set<Enchantment> toRemove = new HashSet<>();
                for (Enchantment enchantment : enchantments.keySet()) {
                    if (EnchantmentTypeManager.getType(enchantment) == this) {
                        toRemove.add(enchantment);
                    }
                }

                toRemove.forEach(itemStack::removeEnchantment);
            }
        }
    }
}
