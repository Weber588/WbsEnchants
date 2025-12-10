package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.enchants.util.EnchantUtils;
import wbs.enchants.util.ItemUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RecoveryEnchant extends WbsEnchantment implements BlockEnchant {
    private static final String DEFAULT_DESCRIPTION = "Books in grindstones have a chance to gain enchantments when " +
            "another item is disenchanted.";
    private static final NamespacedKey RECOVERY_SEED = WbsEnchantsBootstrap.createKey("recovery_seed");

    public RecoveryEnchant() {
        super("recovery", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.GRINDSTONE);
    }

    private int maxEnchantsRecovered = 1;
    private boolean removeCursesOnRecovery = false;
    private final Map<EnchantmentType, Double> recoveryChances = new HashMap<>(Map.of(
            EnchantmentTypeManager.REGULAR, 0.1,
            EnchantmentTypeManager.CURSE, 0d,
            EnchantmentTypeManager.ETHEREAL, 0.05,
            EnchantmentTypeManager.PARADOXICAL, 0.2
    ));

    @Override
    public void configure(@NotNull ConfigurationSection section, String directory) {
        super.configure(section, directory);

        maxEnchantsRecovered = section.getInt("max-chance", maxEnchantsRecovered);
        removeCursesOnRecovery = section.getBoolean("remove-curses-on-recovery", removeCursesOnRecovery);

        ConfigurationSection chancesSection = section.getConfigurationSection("recovery-chances");
        if (chancesSection != null) {
            for (String key : chancesSection.getKeys(false)) {
                EnchantmentType type = EnchantmentTypeManager.getType(WbsEnchantsBootstrap.createKey(key), null);

                if (type != null) {
                    recoveryChances.put(type, chancesSection.getDouble(key, recoveryChances.getOrDefault(type, 0d) * 100));
                }
            }
        }
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getType() == Material.GRINDSTONE;
    }

    @EventHandler
    public void onGrindstone(PrepareGrindstoneEvent event) {
        GrindstoneInventory inventory = event.getInventory();

        Integer level = getLevel(inventory);
        if (level != null) {
            RecoveryGrindstoneInputs inputs = getInputs(inventory);
            if (inputs == null) {
                return;
            }

            // One book, one enchanted item -- simulate the same without the book.
            List<Enchantment> curses = EnchantmentTypeManager.getEnchantmentsOfType(EnchantmentTypeManager.CURSE);
            event.setResult(removeEnchantsExcept(inputs.itemToDisenchant(), curses));
        }
    }

    @EventHandler
    public void onGrindstoneClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!(event.getClickedInventory() instanceof GrindstoneInventory inventory)) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.BOOK) {
                return;
            }

            if (event.isShiftClick()) {
                handleShiftClickBook(event, player, clickedItem);
            }

            return;
        }

        int slot = event.getSlot();

        Integer recoveryLevel = getLevel(inventory);
        if (recoveryLevel == null) {
            return;
        }

        if (slot == 0 || slot == 1) {
            ItemStack cursor = event.getCursor();
            ItemStack currentItem = inventory.getItem(slot);
            if (cursor.getType() == Material.BOOK && (currentItem == null || cursor.getAmount() == 1)) {
                if (cursor.getAmount() == 1) {
                    inventory.setItem(slot, cursor);
                    player.setItemOnCursor(currentItem);
                } else if (currentItem == null) {
                    inventory.setItem(slot, cursor.asOne());
                    player.setItemOnCursor(cursor.subtract());
                }
                // Need to cancel to avoid client seeing existing books in grindstone & behaving differently
                event.setCancelled(true);
            }
        } else if (slot == 2) {// Player is taking the result
            handleTakeItem(event, inventory, recoveryLevel, player);
        }
    }

    private void handleTakeItem(InventoryClickEvent event, GrindstoneInventory inventory, Integer recoveryLevel, Player player) {
        if (recoveryLevel != null) {
            RecoveryGrindstoneInputs inputs = getInputs(inventory);

            if (inputs == null) {
                return;
            }

            int itemHash = ItemUtils.getItemHash(inputs.itemToDisenchant());
            int recoverySeed = getRecoverySeed(player);

            Random random = new Random(recoverySeed + itemHash);

            ItemStack inputItem = inputs.itemToDisenchant();

            Map<Enchantment, Integer> recovered = getEnchantsToRecover(inputItem, random, recoveryLevel);

            ItemStack remainingBook;

            if (recovered.isEmpty()) {
                remainingBook = inputs.bookItem();
            } else {
                remainingBook = ItemStack.of(Material.ENCHANTED_BOOK);
                EnchantUtils.addEnchantments(remainingBook, recovered);
            }

            WbsEnchants.getInstance().runLater(() -> inventory.setItem(inputs.bookSlot(), remainingBook), 1);

            List<Enchantment> toKeep = EnchantmentTypeManager.getEnchantmentsOfType(EnchantmentTypeManager.CURSE);
            if (removeCursesOnRecovery) {
                toKeep.removeAll(recovered.keySet());
            }
            event.setCurrentItem(removeEnchantsExcept(inputItem, toKeep));
            setRecoverySeed(player, random.nextInt());
        }
    }

    private void handleShiftClickBook(InventoryClickEvent event, Player player, ItemStack clickedItem) {
        if (player.getOpenInventory().getTopInventory() instanceof GrindstoneInventory grindstoneInventory) {
            if (!isEnchanted(grindstoneInventory)) {
                return;
            }

            int targetSlot;
            if (grindstoneInventory.getItem(0) == null) {
                targetSlot = 0;
            } else if (grindstoneInventory.getItem(1) == null) {
                targetSlot = 1;
            } else {
                return;
            }

            if (clickedItem.getAmount() == 1) {
                grindstoneInventory.setItem(targetSlot, clickedItem);
                player.getInventory().setItem(event.getSlot(), null);
            } else {
                ItemStack asOne = clickedItem.asOne();
                grindstoneInventory.setItem(targetSlot, asOne);
                player.getInventory().setItem(event.getSlot(), clickedItem.subtract());
            }
            // Need to cancel to avoid client seeing existing books in grindstone & shift clicking them in
            event.setCancelled(true);
            WbsEnchants.getInstance().runLater(player::updateInventory, 1);
        }
    }

    private static int getRecoverySeed(Player player) {
        return player.getPersistentDataContainer().getOrDefault(RECOVERY_SEED, PersistentDataType.INTEGER, player.getEnchantmentSeed());
    }

    private static void setRecoverySeed(Player player, int seed) {
        player.getPersistentDataContainer().set(RECOVERY_SEED, PersistentDataType.INTEGER, seed);
    }

    private @NotNull Map<Enchantment, Integer> getEnchantsToRecover(ItemStack inputItem, Random random, double recoveryLevel) {
        Map<Enchantment, Integer> enchantments = inputItem.getEnchantments();

        Map<Enchantment, Integer> recovered = new HashMap<>();
        // TODO: Ensure this execution order is deterministic, or it may vary between runtimes
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            EnchantmentType type = EnchantmentTypeManager.getType(enchantment);

            double maxChance = recoveryChances.getOrDefault(type, 0d);
            double derivedChance = maxChance * (recoveryLevel / maxLevel());
            double rand = random.nextDouble();
            if (rand < derivedChance) {
                recovered.put(enchantment, level);

                if (maxEnchantsRecovered > 0 && recovered.size() >= maxEnchantsRecovered) {
                    break;
                }
            }
        }
        return recovered;
    }

    private ItemStack removeEnchantsExcept(final ItemStack itemToDisenchant, List<Enchantment> toKeep) {
        ItemStack clone = itemToDisenchant.clone();

        Map<Enchantment, Integer> enchantments = clone.getEnchantments();

        Map<Enchantment, Integer> kept = new HashMap<>();

        enchantments.forEach((enchantment, level) -> {
            if (toKeep.contains(enchantment)) {
                kept.put(enchantment, level);
            }
        });

        clone.removeEnchantments();
        clone.addEnchantments(kept);

        return clone;
    }

    private static @Nullable RecoveryGrindstoneInputs getInputs(GrindstoneInventory inventory) {
        ItemStack upperItem = inventory.getUpperItem(); // slot 0
        ItemStack lowerItem = inventory.getLowerItem(); // slot 1

        if (lowerItem == null || upperItem == null) {
            return null;
        }

        int bookSlot = -1;
        ItemStack bookItem = null;

        if (upperItem.getType() == Material.BOOK) {
            bookItem = upperItem;
            bookSlot = 0;
        }
        if (lowerItem.getType() == Material.BOOK) {
            if (bookItem != null) {
                // Both items are books; skip
                return null;
            }
            bookItem = lowerItem;
            bookSlot = 1;
        }

        if (bookItem == null) {
            return null;
        }

        ItemStack itemToDisenchant;

        if (bookSlot == 0) {
            itemToDisenchant = lowerItem;
        } else {
            itemToDisenchant = upperItem;
        }

        if (itemToDisenchant.getEnchantments().isEmpty()) {
            return null;
        }

        return new RecoveryGrindstoneInputs(bookItem, itemToDisenchant, bookSlot);
    }

    private record RecoveryGrindstoneInputs(ItemStack bookItem, ItemStack itemToDisenchant, int bookSlot) {
    }
}
