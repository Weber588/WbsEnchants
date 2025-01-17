package wbs.enchants.events;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.tag.Tag;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LootGenerationBlocker implements Listener {
    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Tag<@NotNull Enchantment> tag = registry.getTag(EnchantmentTagKeys.ON_RANDOM_LOOT);
    private final Map<Enchantment, Integer> weightedLootEnchants;

    public LootGenerationBlocker() {
        weightedLootEnchants = new HashMap<>();

        tag.values().stream()
                .map(registry::get)
                .filter(Objects::nonNull)
                .forEach(ench ->
                        weightedLootEnchants.put(ench, ench.getWeight())
                );
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLootGenerate(LootGenerateEvent event) {
        event.getLoot().removeIf(item -> {
            replaceNonLootEnchants(item.getEnchantments(), item, item::removeEnchantment, item::addEnchantment);

            item.editMeta(EnchantmentStorageMeta.class, meta -> {
                replaceNonLootEnchants(meta.getStoredEnchants(), item, meta::removeStoredEnchant, (enchantment, level) -> meta.addStoredEnchant(enchantment, 1, false));
            });

            return item.getType() == Material.ENCHANTED_BOOK &&
                    item.getItemMeta() instanceof EnchantmentStorageMeta meta &&
                    meta.getStoredEnchants().isEmpty();
        });
    }


    @EventHandler
    public void onBrushBlock(PlayerInteractEvent event) {
        ItemStack heldItem = event.getItem();
        if (heldItem != null && heldItem.getType() == Material.BRUSH) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getState() instanceof BrushableBlock state) {
                ItemStack spawnedItem = state.getItem();

                boolean changed = replaceNonLootEnchants(spawnedItem.getEnchantments(), spawnedItem, spawnedItem::removeEnchantment, spawnedItem::addEnchantment);

                if (changed) {
                    state.setItem(spawnedItem);
                    state.update();
                }
            }
        }
    }

    private boolean replaceNonLootEnchants(Map<Enchantment, Integer> enchantments, ItemStack target, Consumer<Enchantment> removeConsumer, BiConsumer<Enchantment, Integer> addConsumer) {
        boolean anyChanges = false;

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer level = entry.getValue();
            // Enchantment is not in tag to be added to random loot -- a datapack has added this using "minecraft:enchant_randomly" function
            // which does not respect that tag. Forcing it to below.
            if (!tag.contains(TypedKey.create(RegistryKey.ENCHANTMENT, enchantment.getKey()))) {
                anyChanges = true;
                removeConsumer.accept(enchantment);
                Enchantment replacement = getRandomLootEnchant(target);
                if (replacement != null) {
                    addConsumer.accept(replacement, Math.min(level, replacement.getMaxLevel()));
                }
            }
        }

        return anyChanges;
    }

    private @Nullable Enchantment getRandomLootEnchant(ItemStack target) {
        Map<Enchantment, Integer> compatible = new HashMap<>();

        weightedLootEnchants.forEach((enchant, weight) -> {
            if (enchant.canEnchantItem(target)) {
                compatible.put(enchant, weight);
            }
        });

        if (compatible.isEmpty()) {
            return null;
        }

        return WbsCollectionUtil.getRandomWeighted(compatible);
    }
}
