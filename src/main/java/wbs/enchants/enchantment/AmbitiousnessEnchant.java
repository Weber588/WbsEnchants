package wbs.enchants.enchantment;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.NamespacedKey;
import org.bukkit.block.EnchantingTable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockStateEnchant;
import wbs.enchants.enchantment.helper.EnchantingEnchant;
import wbs.enchants.events.enchanting.*;
import wbs.enchants.util.EnchantingEventUtils;
import wbs.enchants.util.ItemUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AmbitiousnessEnchant extends WbsEnchantment implements EnchantingEnchant, BlockStateEnchant<EnchantingTable> {
    @Override
    public Class<EnchantingTable> getStateClass() {
        return EnchantingTable.class;
    }

    private static final @NotNull String DEFAULT_DESCRIPTION = "Increases the maximum enchanting level, and allows " +
            "you to exceed the maximum level for some enchantments.";

    public AmbitiousnessEnchant() {
        super("ambitiousness", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.ENCHANTING_TABLE)
                .exclusiveInject(WbsEnchantsBootstrap.EXCLUSIVE_SET_ENCHANTING_TABLE);
    }

    private double chanceToExceedAtMax = 50;
    private final List<TypedKey<Enchantment>> allowExceedingMaxLevel = new LinkedList<>();
    private final List<TypedKey<Enchantment>> preventExceedingMaxLevel = new LinkedList<>();

    @Override
    public void configure(@NotNull ConfigurationSection section, String directory) {
        super.configure(section, directory);

        chanceToExceedAtMax = section.getDouble("chance-to-exceed-max-level", chanceToExceedAtMax);

        List<String> allowStrings = section.getStringList("allow-exceeding-max-level");
        for (String allowString : allowStrings) {
            NamespacedKey key = NamespacedKey.fromString(allowString);

            if (key != null) {
                allowExceedingMaxLevel.add(RegistryKey.ENCHANTMENT.typedKey(key));
            }
        }

        List<String> preventStrings = section.getStringList("prevent-exceeding-max-level");
        for (String preventString : preventStrings) {
            NamespacedKey key = NamespacedKey.fromString(preventString);

            if (key != null) {
                preventExceedingMaxLevel.add(RegistryKey.ENCHANTMENT.typedKey(key));
            }
        }

    }

    @EventHandler
    public void onChoosePower(EnchantingDerivePowerEvent event) {
        EnchantingPreparationContext context = event.getContext();

        Integer level = getLevel(context.enchantingBlock());
        if (level != null) {
            event.setMaxPower(getMaxPower(level));
        }
    }

    private static int getMaxCost(int level) {
        return getMaxPower(level) * 2;
    }

    private static int getMaxPower(int level) {
        return EnchantingEventUtils.DEFAULT_MAX_POWER + 5 * level;
    }

    private boolean shouldBreakMaxLevel(int seed, int tableLevel, int cost) {
        double chanceFromLevel = ((double) tableLevel / maxLevel()) / 2;
        double chanceFromCost = ((double) cost / getMaxCost(tableLevel)) / 2;

        double chance = (chanceFromCost + chanceFromLevel) * (chanceToExceedAtMax / 100);

        return new Random(seed).nextDouble() > chance;
    }

    @EventHandler
    public void onSelectEnchants(SelectEnchantmentsEvent event) {
        EnchantingContext context = event.getContext();

        Integer tableLevel = getLevel(context.enchantingBlock());
        if (tableLevel == null) {
            return;
        }

        int seed = context.seed();
        int cost = event.getCost();
        ItemStack item = context.item();
        int slot = event.getSlot();

        Map<Enchantment, Integer> enchantments = event.getEnchantments();

        int itemHash = ItemUtils.getItemHash(item);

        for (Enchantment enchantment : enchantments.keySet()) {
            TypedKey<Enchantment> typedKey = RegistryKey.ENCHANTMENT.typedKey(enchantment.key());
            if (preventExceedingMaxLevel.contains(typedKey)) {
                // Skip if prevented
                continue;
            }
            if (!allowExceedingMaxLevel.isEmpty()) {
                if (!allowExceedingMaxLevel.contains(typedKey)) {
                    // Skip if not in allow list, unless it's empty
                    continue;
                }
            }

            int enchantLevel = enchantments.get(enchantment);
            boolean shouldBreakMaxLevel = shouldBreakMaxLevel(
                    seed + slot + itemHash + enchantment.getKey().hashCode(),
                    tableLevel,
                    cost
            );
            if (enchantment.getMaxLevel() > 1 && shouldBreakMaxLevel) {
                enchantments.put(enchantment, enchantLevel + 1);
            }
        }
    }

    @EventHandler
    public void generationCheckEvent(EnchantmentGenerationCheckEvent event) {
        EnchantingContext context = event.getContext();

        if (isEnchanted(context.enchantingBlock())) {
            event.setAllowed(event.getModifiedEnchantingLevel() >= event.getEnchantment().getMinModifiedCost(event.getEnchantmentLevel()));
        }
    }
}
