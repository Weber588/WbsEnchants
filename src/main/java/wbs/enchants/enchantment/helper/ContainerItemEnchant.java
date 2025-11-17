package wbs.enchants.enchantment.helper;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public interface ContainerItemEnchant extends BundleEnchant, ShulkerBoxEnchantment {
    default @NotNull List<ContainerItemWrapper> getContainerItemWrappers(Player player) {
        List<ContainerItemWrapper> autoPickupItems = new LinkedList<>();

        autoPickupItems.addAll(getEnchantedBoxes(player));
        autoPickupItems.addAll(getEnchantedBundles(player));

        return autoPickupItems;
    }
}
