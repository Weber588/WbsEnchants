package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.PotionContents;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.events.enchanting.*;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class HyperchargedEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Enchanting a water bottle will take levels equal to the requirement, " +
            "instead of just 3. Those levels will be stored in it as an enchanted bottle.";

    public HyperchargedEnchant() {
        super("hypercharged", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_HYPERCHARGED)
                .maxLevel(3)
                .weight(10);
    }


    @EventHandler()
    public void onXPBottle(ExpBottleEvent event) {
        ItemStack item = event.getEntity().getItem();
        int level = getLevel(item);
        if (level > 0) {
            event.setExperience(getExpForLevel(level * 30));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        Enchantment serverEnchantment = getEnchantment();
        Integer level = enchantsToAdd.get(serverEnchantment);
        if (level != null && level > 0) {
            Player player = event.getEnchanter();
            int costTaken = event.whichButton() + 1;
            int levelsToTake = level * 30;
            WbsEnchants.getInstance().runLater(() -> player.giveExpLevels(-levelsToTake + costTaken), 1);
        }
    }

    private static int getExpForLevel(int level) {
        if (level <= 16) {
            return (level + 6) * level;
        } else if (level <= 31) {
            return (int) ((2.5 * level * level) - (40.5 * level) + 360);
        } else {
            return (int) (4.5 * (level * level) - (162.5 * level) + 2200);
        }
    }

    @EventHandler
    public void onSelectEnchantments(EnchantmentGenerationCheckEvent event) {
        EnchantingContext context = event.getContext();
        ItemStack item = context.item();
        if (event.getEnchantment().equals(getEnchantment()) && (item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK)) {
            event.setAllowed(false);
        }
    }

    @EventHandler
    public void onFinalizeEnchants(FinalizeItemEnchantmentsEvent event) {
        Map<Enchantment, Integer> enchantments = event.getEnchantments();

        if (enchantments.containsKey(this.getEnchantment())) {
            EnchantItemEvent wrappedEvent = event.getWrappedEvent();

            if (wrappedEvent.whichButton() + 1 != enchantments.get(getEnchantment())) {
                // Something else is interfering (like Ambitiousness or Powerlust)
                // TODO: Find a better solution than this
                enchantments.put(getEnchantment(), wrappedEvent.whichButton() + 1);
            }

            EnchantingEnchantContext context = event.getContext();
            ItemStack experienceBottleItem = context.item().withType(Material.EXPERIENCE_BOTTLE);
            experienceBottleItem.resetData(DataComponentTypes.POTION_CONTENTS);
            wrappedEvent.setItem(experienceBottleItem);
        }
    }

    @EventHandler
    public void onEnchantWaterBottle(SelectEnchantmentsEvent event) {
        if (!(event.getContext() instanceof EnchantingPreparationContext context)) {
            return;
        }

        Map<Enchantment, Integer> enchantments = event.getEnchantments();

        Enchantment serverEnchantment = this.getEnchantment();
        if (!enchantments.containsKey(serverEnchantment)) {
            return;
        }

        enchantments.remove(serverEnchantment);

        ItemStack item = context.item();
        if (serverEnchantment.canEnchantItem(item) && item.getType() != Material.BOOK && item.getType() != Material.ENCHANTED_BOOK) {
            PotionContents potionContents = item.getData(DataComponentTypes.POTION_CONTENTS);
            if (potionContents != null && !potionContents.allEffects().isEmpty()) {
                return;
            }

            Integer slot = context.slot();

            if (slot == null || (slot + 1) > maxLevel()) {
                return;
            }

            enchantments.put(
                    serverEnchantment,
                    slot + 1
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantWaterBottle(ChooseEnchantmentCostEvent event) {
        EnchantingPreparationContext context = event.getContext();

        ItemStack item = context.item();
        if (getEnchantment().canEnchantItem(item) && item.getType() != Material.BOOK && item.getType() != Material.ENCHANTED_BOOK) {
            PotionContents potionContents = item.getData(DataComponentTypes.POTION_CONTENTS);
            if (potionContents != null && !potionContents.allEffects().isEmpty()) {
                return;
            }

            Integer slot = context.slot();

            if (slot == null) {
                return;
            }

            event.setCost(switch (slot) {
                case 0 -> 30;
                case 1 -> 60;
                case 2 -> 90;
                default -> throw new IllegalStateException("Unexpected value: " + slot);
            });
        }
    }
}
