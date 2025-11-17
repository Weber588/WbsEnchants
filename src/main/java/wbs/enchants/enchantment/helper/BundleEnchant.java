package wbs.enchants.enchantment.helper;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.BundleContents;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public interface BundleEnchant extends EnchantInterface {
    @Nullable
    @Contract("null -> null")
    default BundleWrapper getBundle(ItemStack item) {
        if (item == null) {
            return null;
        }

        BundleContents bundleContents = item.getData(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return null;
        }

        return new BundleWrapper(bundleContents, item);
    }

    default List<BundleWrapper> getEnchantedBundles(Player player) {
        List<BundleWrapper> enchantedBundles = new LinkedList<>();
        for (ItemStack item : player.getInventory()) {
            if (item != null && getThisEnchantment().isEnchantmentOn(item)) {
                @Nullable BundleWrapper wrapper = getBundle(item);
                if (wrapper != null) {
                    enchantedBundles.add(wrapper);
                }
            }
        }

        return enchantedBundles;
    }

    @SuppressWarnings("UnstableApiUsage")
    class BundleWrapper extends ContainerItemWrapper {
        private final @NotNull List<ItemStack> contents;
        private static final int MAX_BUNDLE_SLOTS = 64;

        public BundleWrapper(@NotNull BundleContents contents, @NotNull ItemStack item) {
            super(item);
            this.contents = new LinkedList<>(contents.contents());
        }

        @Override
        public void saveToItem() {
            item().setData(DataComponentTypes.BUNDLE_CONTENTS, BundleContents.bundleContents().addAll(contents).build());
        }

        @Override
        public boolean canContain(ItemStack check) {
            if (!super.canContain(check)) {
                return false;
            }

            return !(Tag.ITEMS_BUNDLES.isTagged(item().getType()) && Tag.ITEMS_BUNDLES.isTagged(check.getType()));
        }

        @Override
        public boolean containsAtLeast(ItemStack item, int amountRequired) {
            if (item == null) {
                return false;
            } else if (amountRequired <= 0) {
                return true;
            } else {
                for(ItemStack i : contents) {
                    if (item.isSimilar(i) && (amountRequired -= i.getAmount()) <= 0) {
                        return true;
                    }
                }

                return false;
            }
        }

        public int slotsUsed() {
            return contents.stream()
                    .mapToInt(BundleWrapper::getSlotsTaken)
                    .sum();
        }

        public static int getSlotsTaken(ItemStack itemStack) {
            int amount = itemStack.getAmount();
            int slotsTaken = getSlotsPerItem(itemStack);
            return amount * slotsTaken;
        }

        private static int getSlotsPerItem(ItemStack itemStack) {
            return Math.max(1, MAX_BUNDLE_SLOTS / itemStack.getMaxStackSize());
        }

        @Override
        public ItemStack addItem(ItemStack other) {
            int slotsUsed = slotsUsed();
            if (slotsUsed >= MAX_BUNDLE_SLOTS) {
                return other;
            }

            int remainingSpace = MAX_BUNDLE_SLOTS - slotsUsed;

            int amountToAdd = 0;

            int slotsPerItem = getSlotsPerItem(other);
            while (remainingSpace >= slotsPerItem && amountToAdd < other.getAmount()) {
                remainingSpace -= slotsPerItem;
                amountToAdd++;
            }

            ItemStack toAdd = other.asQuantity(amountToAdd);

            for (ItemStack content : contents) {
                if (content.isSimilar(toAdd)) {
                    int amountRemaining = content.getMaxStackSize() - content.getAmount();

                    while (amountRemaining > 0 && toAdd.getAmount() > 0) {
                        amountRemaining--;
                        toAdd.subtract();
                        content.add();
                    }
                }
            }

            if (toAdd.getAmount() > 0) {
                contents.add(toAdd);
            }

            int amountLeftover = other.getAmount() - amountToAdd;

            if (amountLeftover > 0) {
                return other.asQuantity(amountLeftover);
            }

            return null;
        }

        @Override
        public ItemStack[] getItems() {
            return contents.toArray(ItemStack[]::new);
        }

        @Override
        public void removeItem(ItemStack stack) {
            List<ItemStack> toRemove = new LinkedList<>();
            List<ItemStack> toAdd = new LinkedList<>();

            int amountToRemove = stack.getAmount();
            for (ItemStack content : contents) {
                int contentAmount = content.getAmount();
                int removedThisItem;

                if (amountToRemove >= contentAmount) {
                    amountToRemove -= contentAmount;
                    removedThisItem = contentAmount;
                } else {
                    removedThisItem = amountToRemove;
                    amountToRemove = 0;
                }

                if (removedThisItem > 0) {
                    toRemove.add(content);
                    int remaining = contentAmount - removedThisItem;
                    if (remaining > 0) {
                        toAdd.add(content.asQuantity(remaining));
                    }
                }

                if (amountToRemove <= 0) {
                    break;
                }
            }

            contents.removeAll(toRemove);
            contents.addAll(toAdd);
        }
    }
}
