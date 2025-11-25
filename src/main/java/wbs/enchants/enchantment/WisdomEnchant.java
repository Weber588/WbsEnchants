package wbs.enchants.enchantment;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

public class WisdomEnchant extends WbsEnchantment {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Removes XP pickup cooldown.";
    public WisdomEnchant() {
        super("wisdom", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR)
                .activeSlots(EquipmentSlotGroup.HEAD)
                .maxLevel(1)
                .weight(1)
                .anvilCost(1);
    }

    @EventHandler
    public void onPickupXP(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = getHighestEnchanted(player);
        if (item != null) {
            ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
            player.getNearbyEntities(1, 2, 1)
                    .stream()
                    .filter(ExperienceOrb.class::isInstance)
                    .map(ExperienceOrb.class::cast)
                    .forEach(orb -> {
                        player.giveExp(orb.getExperience(), true);
                        orb.remove();
                    });

                    /*
                    .filter(CraftExperienceOrb.class::isInstance)
                    .filter(orb -> !event.getExperienceOrb().getUniqueId().equals(orb.getUniqueId()))
                    .map(CraftExperienceOrb.class::cast)
                    .map(CraftExperienceOrb::getHandle)
                    .forEach(orb -> {
                        serverPlayer.takeXpDelay = 0;
                        orb.playerTouch(serverPlayer);
                    });

                     */
        }
    }
}
