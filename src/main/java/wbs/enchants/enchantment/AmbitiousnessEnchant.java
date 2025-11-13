package wbs.enchants.enchantment;

import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.block.Block;
import org.bukkit.block.EnchantingTable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.view.EnchantmentView;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;
import wbs.utils.util.WbsMath;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class AmbitiousnessEnchant extends WbsEnchantment implements BlockStateEnchant<EnchantingTable> {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Increases the maximum enchanting level.";
    public static final int DEFAULT_MAX_BOOKSHELVES = 15;
    public static final int IDEAL_MAX_BOOKSHELVES = 22;

    public AmbitiousnessEnchant() {
        super("ambitiousness", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(2)
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

        int[] levels = getModifiedLevels(event, level);

        for (int i = 0; i < 3; i++) {
            EnchantmentOffer offer = event.getOffers()[i];
            if (offer != null) {
                offer.setCost(levels[i]);

                int offerLevel = offer.getEnchantmentLevel();
                if (offer.getEnchantment().getMaxLevel() > offerLevel && WbsMath.chance(level * 25)) {
                    offer.setEnchantmentLevel(offerLevel + 1);
                }
            }
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Block block = event.getEnchantBlock();

        if (!isEnchanted(block)) {
            return;
        }

        Enchantment enchantmentHint = event.getEnchantmentHint();
        int levelHint = event.getLevelHint();

        event.getItem().addEnchantment(enchantmentHint, levelHint);
        Set<Enchantment> remove = new HashSet<>();
        Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        for (Enchantment enchantment : enchantsToAdd.keySet()) {
            if (enchantment.conflictsWith(enchantmentHint)) {
                remove.add(enchantment);
            }
        }

        for (Enchantment enchantment : remove) {
            enchantsToAdd.remove(enchantment);
        }
    }

    private int @NotNull [] getModifiedLevels(PrepareItemEnchantEvent event, int level) {
        Random random = new Random(event.getEnchanter().getEnchantmentSeed());

        int maxBookshelves = DEFAULT_MAX_BOOKSHELVES + (IDEAL_MAX_BOOKSHELVES - DEFAULT_MAX_BOOKSHELVES) * level / maxLevel();
        int power = Math.min(event.getEnchantmentBonus(), maxBookshelves);
        double modifiedPower = (double) power * (DEFAULT_MAX_BOOKSHELVES + level * 5) / maxBookshelves;
        int base = (int) ((random.nextInt(6) + 1) + modifiedPower + random.nextInt((int) (modifiedPower + 1)));

        return new int[]{
                Math.max(base / 3, 1),
                base * 2 / 3 + 1,
                (int) Math.min(Math.max(base, modifiedPower * 2), 30 + level * 10)
        };
    }
}
