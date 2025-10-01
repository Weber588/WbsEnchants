package wbs.enchants.enchantment;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

public class PlatformerEnchant extends WbsEnchantment {
    private static final NamespacedKey PLATFORM_JUMPS = WbsEnchantsBootstrap.createKey("platform_jumps");

    private static final @NotNull String DEFAULT_DESCRIPTION = "Adds the ability to jump mid-air, once per level of the enchantment.";

    public PlatformerEnchant() {
        super("platformer", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_FOOT_ARMOR)
                .description(Component.text(DEFAULT_DESCRIPTION)
                        .appendNewline()
                        .append(Component.text("Warning: Highly affected by ping/lag. Not recommended on high latency connections.")
                                .color(NamedTextColor.RED)
                        )
                );
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        int level = getSumLevels(player);
        if (level > 0) {
            player.getPersistentDataContainer().remove(PLATFORM_JUMPS);
        }
    }

    @EventHandler
    public void onJumpInAir(PlayerInputEvent event) {
        Player player = event.getPlayer();
        if (!event.getInput().isJump() || player.getCurrentInput().isJump()) {
            return;
        }

        int extraJumps = getSumLevels(player);
        if (extraJumps > 0) {
            PersistentDataContainer container = player.getPersistentDataContainer();

            Integer currentJumps = container.get(PLATFORM_JUMPS, PersistentDataType.INTEGER);

            // Needed because this event fires after jump, and so it'll try to use it on the first jump.
            if (currentJumps == null) {
                container.set(PLATFORM_JUMPS, PersistentDataType.INTEGER, 0);
            } else {
                if (currentJumps < extraJumps) {
                    Vector currentVelocity = player.getVelocity();
                    // TODO: Make this read from player jump speed (unless we can derive it from more direct means)
                    player.setVelocity(currentVelocity.setY(0.3));
                    container.set(PLATFORM_JUMPS, PersistentDataType.INTEGER, currentJumps + 1);
                }
            }
        }
    }
}
