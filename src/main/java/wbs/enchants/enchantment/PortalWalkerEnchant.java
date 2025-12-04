package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.minecraft.world.entity.PortalProcessor;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;

import java.lang.reflect.Field;

public class PortalWalkerEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Allows you to instantly travel through nether portals.";

    public PortalWalkerEnchant() {
        super("portal_walker", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_LEG_ARMOR)
                .minimumCost(25, 0)
                .maximumCost(55, 0)
                .weight(1)
                .activeSlots(EquipmentSlotGroup.ARMOR)
                .anvilCost(6);
    }

    @EventHandler
    public void onPortalEnter(EntityPortalEnterEvent event) {
        if (event.getEntity() instanceof CraftPlayer player) {
            if (getHighestEnchanted(player) == null) {
                return;
            }

            PortalProcessor portalProcess = player.getHandle().portalProcess;
            if (portalProcess != null) {
                try {
                    Field portalTimeField = PortalProcessor.class.getDeclaredField("portalTime");

                    portalTimeField.setAccessible(true);

                    World world = player.getWorld();
                    Integer defaultPortalDelay = getGameRule(GameRule.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY, world, 300);
                    Integer creativePortalDelay = getGameRule(GameRule.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY, world, 0);

                    portalTimeField.set(portalProcess, Math.max(
                            defaultPortalDelay,
                            creativePortalDelay
                    ));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Nullable
    private <T> T getGameRule(GameRule<T> gameRule, World world) {
        return getGameRule(gameRule, world, null);
    }

    @Nullable
    @Contract("_, _, !null -> !null")
    private <T> T getGameRule(GameRule<T> gameRule, World world, T fallback) {
        T value = world.getGameRuleValue(gameRule);
        if (value == null) {
            T defaultValue = world.getGameRuleDefault(gameRule);

            if (defaultValue == null) {
                return fallback;
            }
            return defaultValue;
        }

        return value;
    }
}
