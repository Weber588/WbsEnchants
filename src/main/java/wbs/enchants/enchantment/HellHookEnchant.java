package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.FishingEnchant;
import wbs.enchants.util.ItemUtils;

public class HellHookEnchant extends WbsEnchantment implements FishingEnchant {
    private static final String DEFAULT_DESCRIPTION = "Sets your fishing rod hook on fire, burning entities and " +
            "cooking anything you fish up.";

    public HellHookEnchant() {
        super("hell_hook", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.FISHING_ROD);
    }

    @Override
    public void onHookHit(ProjectileHitEvent event, @NotNull Entity hit, ItemStack rod, EquipmentSlot hand) {
        int level = getLevel(rod);

        hit.setFireTicks(level * 20);
    }

    @Override
    public void onFishEvent(PlayerFishEvent event, ItemStack rod, EquipmentSlot hand) {
        int level = getLevel(rod);

        FishHook hook = event.getHook();
        switch (event.getState()) {
            case FISHING -> {
                hook.setVisualFire(TriState.TRUE);
                hook.setFireTicks(hook.getMaxFireTicks());
            }
            case CAUGHT_FISH -> {
                Entity caught = event.getCaught();
                if (caught instanceof Item item) {
                    ItemStack smelted = ItemUtils.smeltItem(item.getItemStack());

                    if (smelted != null) {
                        item.setItemStack(smelted);
                    }
                }
            }
            case CAUGHT_ENTITY -> {
                Entity caught = event.getCaught();
                if (caught != null) {
                    caught.setFireTicks(level * 20);
                }
            }
        }
    }
}
