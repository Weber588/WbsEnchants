package wbs.enchants.enchantment;

import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Bukkit;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.view.EnchantmentView;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;
import wbs.enchants.type.EnchantmentTypeManager;

import java.util.Map;
import java.util.Random;

@SuppressWarnings("UnstableApiUsage")
public class PowerlustEnchant extends WbsEnchantment implements BlockStateEnchant<EnchantingTable> {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Allows generated enchantments to exceed their maximum level by 1, " +
            "but takes more levels when enchanting.";
    public static final int CHANCE_TO_IMPROVE = 30;

    public PowerlustEnchant() {
        super("powerlust", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .type(EnchantmentTypeManager.PARADOXICAL)
                .supportedItems(ItemTypeKeys.ENCHANTING_TABLE)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_ENCHANTING_TABLE);
    }

    @Override
    public Class<EnchantingTable> getStateClass() {
        return EnchantingTable.class;
    }

    @EventHandler
    public void onOpenEnchantingTable(InventoryOpenEvent event) {
        if (!(event.getView() instanceof EnchantmentView view)) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof TileStateInventoryHolder holder)) {
            return;
        }

        if (!isEnchanted(holder.getBlock())) {
            return;
        }

        HumanEntity player = event.getPlayer();
        // Add hashcode so enchants on tables of this enchantment won't just be a copy of vanilla enchants but better levels.
        view.setEnchantmentSeed(new Random(player.getEnchantmentSeed() + hashCode()).nextInt());
    }

    @EventHandler
    public void onPrepEnchant(PrepareItemEnchantEvent event) {
        Integer level = getLevel(event.getEnchantBlock());
        if (level == null) {
            return;
        }

        Random random = new Random(event.getView().getEnchantmentSeed());

        for (int i = 0; i < 3; i++) {
            EnchantmentOffer offer = event.getOffers()[i];
            if (offer != null) {
                // Don't increase levels on enchants that only support 1.
                if (offer.getEnchantment().getMaxLevel() > 1) {
                    int offerLevel = offer.getEnchantmentLevel();
                    if (random.nextDouble() * 100 > CHANCE_TO_IMPROVE) {
                        offer.setEnchantmentLevel(offerLevel + 1);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Integer level = getLevel(event.getEnchantBlock());
        if (level == null) {
            return;
        }

        Enchantment enchantmentHint = event.getEnchantmentHint();
        int levelHint = event.getLevelHint();

        int buffed = 0;

        Player player = event.getEnchanter();
        Random random = new Random(player.getEnchantmentSeed());

        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        for (Enchantment enchantment : enchantsToAdd.keySet()) {
            // The enchantment hint was pre-calculated in the prep event -- trust it.
            if (enchantment.equals(enchantmentHint)) {
                enchantsToAdd.put(enchantment, levelHint);
                buffed++;
                if (levelHint > enchantment.getMaxLevel()) {
                    buffed++;
                }
            } else {
                // Don't increase levels on enchants that only support 1.
                if (enchantment.getMaxLevel() > 1) {
                    int offerLevel = enchantsToAdd.get(enchantment);
                    if (random.nextDouble() * 100 > CHANCE_TO_IMPROVE) {
                        buffed++;
                        enchantsToAdd.put(enchantment, offerLevel + 1);
                        if (offerLevel + 1 > enchantment.getMaxLevel()) {
                            buffed++;
                        }
                    }
                }
            }
        }

        int finalBuffed = buffed;
        WbsEnchants.getInstance().runLater(() -> {
            int defaultLevelsToTake = (event.whichButton() + 1);
            Player currentPlayer = Bukkit.getPlayer(player.getUniqueId());
            if (currentPlayer != null) {
                currentPlayer.giveExpLevels(-1 * Math.min(
                        event.getExpLevelCost() - defaultLevelsToTake,
                        defaultLevelsToTake * (finalBuffed + 1)
                ));
            }
        }, 1);
    }
}
