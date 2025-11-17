package wbs.enchants.enchantment.helper;

import net.kyori.adventure.text.Component;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ContainerItemWrapper {
    private final @NotNull ItemStack item;

    public ContainerItemWrapper(@NotNull ItemStack item) {
        this.item = item;
    }

    public Component displayName() {
        return item.effectiveName();
    }

    public abstract void saveToItem();

    public boolean canContain(ItemStack check) {
        if (check == null) {
            return true;
        }

        if (check.equals(item)) {
            return false;
        }

        if (Tag.SHULKER_BOXES.isTagged(check.getType())) {
            return false;
        }

        return true;
    }

    public @NotNull ItemStack item() {
        return item;
    }

    public abstract boolean containsAtLeast(ItemStack drop, int i);

    @Nullable
    public abstract ItemStack addItem(ItemStack other);

    public abstract ItemStack[] getItems();

    public abstract void removeItem(ItemStack stack);
}
