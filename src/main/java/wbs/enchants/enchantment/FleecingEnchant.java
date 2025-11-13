package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.ShearingEnchant;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class FleecingEnchant extends WbsEnchantment implements ShearingEnchant {
    private static final int MAX_EXTRAS_PER_LEVEL = 2;

    private static final String DEFAULT_DESCRIPTION = "Increases yield of wool when shearing.";

    public FleecingEnchant() {
        super("fleecing", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.SHEARS)
                .minimumCost(15, 9)
                .maximumCost(65, 9);
    }

    @Override
    public void onShearSheep(ShearEntityEvent event) {
        ItemStack tool = event.getTool();
        List<ItemStack> extraDrops = new LinkedList<>();
        int level = getLevel(tool);

        Random random = new Random();
        List<ItemStack> drops = event.getDrops();
        for (ItemStack drop : drops) {
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

        drops.addAll(extraDrops);
        event.setDrops(drops);
    }
}
