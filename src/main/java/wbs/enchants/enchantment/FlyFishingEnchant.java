package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.FishingEnchant;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.WbsMath;

import java.util.Collection;

public class FlyFishingEnchant extends WbsEnchantment implements FishingEnchant {
    private static final double CHANCE_PER_LEVEL = 20;
    private static final @NotNull String DEFAULT_DESCRIPTION = "Makes the window for catching fish shorter, but has " +
            "a chance to get extra loot when successful.";

    public FlyFishingEnchant() {
        super("fly_fishing", DEFAULT_DESCRIPTION);

        getDefinition()
                .type(EnchantmentTypeManager.PARADOXICAL)
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_FISHING)
                .exclusiveWith(EnchantManager.AUTO_REEL)
                .maxLevel(3);
    }

    @Override
    public void onFishEvent(PlayerFishEvent event, ItemStack rod, EquipmentSlot hand) {
        int level = getLevel(rod);

        FishHook hook = event.getHook();
        final Player player = event.getPlayer();

        switch (event.getState()) {
            case PlayerFishEvent.State.CAUGHT_FISH -> {
                if (!WbsMath.chance(CHANCE_PER_LEVEL * level)) {
                    return;
                }

                Item existingItem = (Item) event.getCaught();

                if (existingItem == null) {
                    return;
                }

                NamespacedKey fishingKey = NamespacedKey.minecraft("gameplay/fishing");
                LootTable lootTable = Bukkit.getLootTable(fishingKey);

                if (lootTable == null) {
                    WbsEnchants.getInstance().getLogger().severe("Fishing loot table was missing! " + fishingKey);
                    return;
                }

                LootContext context = new LootContext.Builder(hook.getLocation())
                        .killer(player)
                        .luck(getLuck(hook))
                        .build();

                Collection<ItemStack> bonusItems = lootTable.populateLoot(null, context);

                if (!bonusItems.isEmpty()) {
                    for (ItemStack bonusItem : bonusItems) {
                        existingItem.getWorld().dropItem(existingItem.getLocation(), bonusItem, spawned -> {
                            spawned.setVelocity(existingItem.getVelocity());
                        });
                    }
                } else {
                    WbsEnchants.getInstance().getLogger().warning("Fishing loot failed to generate!");
                }
            }
            case PlayerFishEvent.State.BITE -> {
                setNibbleTicksRemaining(hook, getNibbleTicksRemaining(hook) / (level + 1));
            }
        }
    }
}
