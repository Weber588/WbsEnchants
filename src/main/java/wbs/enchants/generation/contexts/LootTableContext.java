package wbs.enchants.generation.contexts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.*;

public class LootTableContext extends ExistingLootContext {

    private List<String> tables = new LinkedList<>();

    public LootTableContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        super(key, definition, section, directory);

        if (section.isList("tables")) {
            tables = section.getStringList("tables");

            if (tables.isEmpty()) {
                throw new InvalidConfigurationException("No valid loot tables provided! Disabling context.", directory);
            }
        }
    }

    @Override
    protected Component describeContext(TextComponent listBreak) {
        if (tables.isEmpty()) {
            return Component.text("In loot containers (chests, barrels, pots, etc)")
                    .append(Component.text(": " + chanceToRun() + "%"));
        }

        List<TextComponent> lootTables = tables.stream()
                .map(Component::text)
                .toList();

        return Component.text("Loot tables matching below ")
                .append(Component.text("RegEx")
                        .clickEvent(ClickEvent.openUrl("https://regexone.com"))
                        .hoverEvent(HoverEvent.showText(Component.text("RegEx is a method for identifying words or phrases with a simple string. Click for more information.")))
                )
                .append(Component.text(": " + chanceToRun() + "%"))
                .append(listBreak)
                .append(Component.join(JoinConfiguration.separator(listBreak), lootTables));
    }

    @Override
    protected int getDefaultChance() {
        return definition.getEnchantment().getWeight() * 3;
    }


    private boolean canAddTo(LootTable table) {
        if (tables.isEmpty()) {
            return true;
        }

        String tableKey = table.getKey().toString();
        String nameKey = table.getKey().getKey();
        for (String tableName : tables) {
            if (tableKey.matches(tableName)) {
                return true;
            }
            if (nameKey.matches(tableName)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onLootTableGenerate(LootGenerateEvent event) {
        if (!shouldRun()) {
            return;
        }

        LootContext context = event.getLootContext();
        Player player = null;
        if (context.getKiller() instanceof Player) {
            player = (Player) context.getKiller();
        } else if (event.getEntity() instanceof Player) {
            player = (Player) event.getEntity();
        }

        if (!meetsAllConditions(context.getLootedEntity(), context.getLocation().getBlock(), context.getLocation(), player)) {
            return;
        }

        if (!canAddTo(event.getLootTable())) {
            return;
        }

        tryAddingTo(event.getLoot());
    }
    
    @EventHandler
    public void onBrushBlock(PlayerInteractEvent event) {
        if (!shouldRun()) {
            return;
        }

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.BRUSH) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getState() instanceof BrushableBlock state) {
                LootTable lootTable = state.getLootTable();

                if (lootTable == null) {
                    return;
                }

                if (!canAddTo(lootTable)) {
                    return;
                }

                Player player = event.getPlayer();

                LootContext.Builder builder = new LootContext.Builder(clickedBlock.getLocation());

                builder.killer(player);

                AttributeInstance luckAttribute = player.getAttribute(Attribute.GENERIC_LUCK);
                if (luckAttribute != null) {
                    builder.luck((float) luckAttribute.getValue());
                }
                // TODO: Maybe use builder.lootingModifier() here with a custom enchant, or just fortune/looting??

                // Since there's no loot generate event fired for brushing a block, fake the loot generation, apply
                // enchant randomly as normal, and then choose an item at random. If it's one we enchanted, set the
                // item directly and override by setting the item inside the block as our item.

                Inventory fake = Bukkit.createInventory(null, 9 * 3, Component.empty());

                lootTable.fillInventory(fake, new Random(), builder.build());

                List<ItemStack> contents = new LinkedList<>(Arrays.asList(fake.getContents()));

                int generated = tryAddingTo(contents);

                if (generated > 0) {
                    // Don't need to find a random one, because tryAddingTo shuffles anyway.
                    ItemStack fakeChosen = contents.stream()
                            .filter(Objects::nonNull)
                            .findAny()
                            .orElseThrow(()
                                    -> new IllegalStateException("Enchantment generated but no non-null item present"));

                    boolean hasEnchantStored = EnchantUtils.getStoredEnchantments(fakeChosen).keySet()
                            .stream()
                            .anyMatch(check -> check.getKey().equals(definition.key()));

                    if (hasEnchantStored || definition.isEnchantmentOn(fakeChosen)) {
                        state.setLootTable(null);
                        state.setItem(fakeChosen);
                        state.update();
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
