package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class FleecingEnchant extends WbsEnchantment {
    private static final int MAX_EXTRAS_PER_LEVEL = 2;

    private static final String DEFAULT_DESCRIPTION = "Increases yield of wool when shearing.";

    public FleecingEnchant() {
        super("fleecing", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.SHEARS);
    }

    @EventHandler
    public void onShearSheep(PlayerShearEntityEvent event) {
        onShearSheep(new ShearEntityEvent(event));
    }

    @EventHandler
    public void onShearSheep(BlockShearEntityEvent event) {
        onShearSheep(new ShearEntityEvent(event));
    }

    private void onShearSheep(ShearEntityEvent event) {
        if (isEnchantmentOn(event.tool)) {
            List<ItemStack> extraDrops = new LinkedList<>();
            int level = getLevel(event.tool);

            Random random = new Random();
            for (ItemStack drop : event.drops) {
                if (Tag.WOOL.isTagged(drop.getType())) {
                    int amountToAdd = 0;
                    for (int i = 0; i < level; i++) {
                        amountToAdd += random.nextInt(MAX_EXTRAS_PER_LEVEL + 1);
                    }

                    if (amountToAdd == 0) {
                        continue;
                    }
                    ItemStack toAdd = drop.clone();
                    toAdd.setAmount(amountToAdd);
                    extraDrops.add(toAdd);
                }
            }

            event.drops.addAll(extraDrops);
            event.setDrops(event.drops);
        }
    }

    private static class ShearEntityEvent {
        private final Consumer<List<ItemStack>> setDrops;
        private final List<ItemStack> drops;
        private final ItemStack tool;

        public ShearEntityEvent(PlayerShearEntityEvent event) {
            setDrops = event::setDrops;
            drops = new LinkedList<>(event.getDrops());
            tool = event.getItem();
        }

        public ShearEntityEvent(BlockShearEntityEvent event) {
            setDrops = event::setDrops;
            drops = new LinkedList<>(event.getDrops());
            tool = event.getTool();
        }

        public void setDrops(List<ItemStack> drops) {
            this.setDrops.accept(drops);
        }
    }
}
