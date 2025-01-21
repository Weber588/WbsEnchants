package wbs.enchants.enchantment.helper;

import net.kyori.adventure.text.Component;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;

public class ShulkerBoxEnchantment extends WbsEnchantment implements BlockEnchant {
    public ShulkerBoxEnchantment(@NotNull String key, @NotNull String description) {
        super("shulker/" + key, WbsStrings.capitalizeAll(key.replaceAll("_", " ")), description);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_SHULKER_BOX);
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getState() instanceof ShulkerBox;
    }

    @Nullable
    @Contract("null -> null")
    protected final ShulkerBoxWrapper getShulkerBox(ItemStack item) {
        if (item == null) {
            return null;
        }

        if (!Tag.SHULKER_BOXES.isTagged(item.getType())) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return null;
        }

        if (!(meta instanceof BlockStateMeta blockStateMeta)) {
            return null;
        }

        if (!(blockStateMeta.getBlockState() instanceof ShulkerBox box)) {
            return null;
        }

        return new ShulkerBoxWrapper(box, item);
    }

    protected final List<ShulkerBoxWrapper> getEnchantedInInventory(Player player) {
        List<ShulkerBoxWrapper> enchantedBoxes = new LinkedList<>();
        for (ItemStack item : player.getInventory()) {
            if (item != null && isEnchantmentOn(item)) {
                ShulkerBoxWrapper wrapper = getShulkerBox(item);
                if (wrapper != null) {
                    enchantedBoxes.add(wrapper);
                }
            }
        }

        return enchantedBoxes;
    }

    public record ShulkerBoxWrapper(@NotNull ShulkerBox box, @NotNull ItemStack item) {
        public Component displayName() {
            // TODO: CHange to effectiveName() in 1.21.4
            ItemMeta meta = item().getItemMeta();
            if (meta.hasDisplayName()) {
                return meta.displayName();
            } else if (meta.hasItemName()) {
                return meta.itemName();
            } else {
                return Component.translatable(item().getType().translationKey(), WbsEnums.toPrettyString(item().getType()));
            }
        }

        public void save() {
            ItemMeta meta = item.getItemMeta();

            if (meta == null) {
                return;
            }

            if (!(meta instanceof BlockStateMeta blockStateMeta)) {
                return;
            }

            blockStateMeta.setBlockState(box);

            item.setItemMeta(blockStateMeta);
        }

        public boolean canContain(ItemStack check) {
            if (check == null) {
                return true;
            }

            if (check.equals(item)) {
                return false;
            }

            return !Tag.SHULKER_BOXES.isTagged(check.getType());
        }
    }
}
