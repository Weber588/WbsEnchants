package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.WbsLocationUtil;
import wbs.utils.util.entities.WbsEntityUtil;

import java.util.Map;
import java.util.Set;

public class HoveringEnchant extends WbsEnchantment implements TickableEnchant {
    public static final int MAX_HEIGHT = 8;
    private static final @NotNull String DEFAULT_DESCRIPTION = "Creative-mode flight while within " + MAX_HEIGHT + " blocks of the ground, but you still take fall damage.";

    public HoveringEnchant() {
        super("hovering", EnchantmentTypeManager.ETHEREAL, DEFAULT_DESCRIPTION);

        getDefinition()
                .activeSlots(EquipmentSlotGroup.ARMOR)
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_ELYTRA);
    }

    @Override
    public int getTickFrequency() {
        return 10;
    }

    // Using global tick to catch players that no longer have the item on them/equipped
    @Override
    public void onGlobalTick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (canNaturallyFly(player)) {
                return;
            }

            PersistentDataContainer container = player.getPersistentDataContainer();
            if (container.has(getKey())) {
                if (getSumLevels(player) <= 0) {
                    clearFlight(player);
                }
            }
        }
    }

    @Override
    public void onTickEquipped(LivingEntity owner, Map<ItemStack, EquipmentSlot> enchantedStacks) {
        if (!(owner instanceof Player player)) {
            return;
        }
        if (canNaturallyFly(player)) {
            return;
        }

        Set<Block> intersectingBlocks = WbsLocationUtil.getIntersectingBlocks(player.getBoundingBox().expand(MAX_HEIGHT), WbsEntityUtil.getMiddleLocation(player));

        boolean canHover = intersectingBlocks.stream().anyMatch(Block::isSolid);

        if (canHover) {
            allowFlight(player);
        } else {
            clearFlight(player);
        }
    }

    private void allowFlight(Player player) {
        player.setAllowFlight(true);
        player.setFlyingFallDamage(TriState.TRUE);
        player.getPersistentDataContainer().set(getKey(), PersistentDataType.BOOLEAN, true);
    }

    private void clearFlight(Player player) {
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setFlying(false);
            player.setAllowFlight(false);
            player.setFlyingFallDamage(TriState.NOT_SET);

            for (ItemStack armorItem : player.getEquipment().getArmorContents()) {
                //noinspection UnstableApiUsage
                if (armorItem != null && armorItem.hasData(DataComponentTypes.GLIDER)) {
                    player.setGliding(true);
                    break;
                }
            }
        }
        player.getPersistentDataContainer().remove(getKey());
    }

    private static boolean canNaturallyFly(Player player) {
        return player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR;
    }
}
