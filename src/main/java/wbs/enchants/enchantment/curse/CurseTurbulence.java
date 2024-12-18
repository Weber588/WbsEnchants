package wbs.enchants.enchantment.curse;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.type.EnchantmentType;
import wbs.enchants.type.EnchantmentTypeManager;
import wbs.utils.util.WbsMath;

public class CurseTurbulence extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "An elytra curse that causes turbulence while flying, " +
            "making it harder to stay on track!";

    private static final int CHANCE_PER_LEVEL_PER_TICK = 1;

    public CurseTurbulence() {
        super("curse/turbulence", DEFAULT_DESCRIPTION);

        supportedItems = WbsEnchantsBootstrap.ELYTRA;
        maxLevel = 2;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Turbulence";
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        ItemStack enchantedItem = getIfEnchanted(player, EquipmentSlot.CHEST);
        if (enchantedItem != null && enchantedItem.getType() == Material.ELYTRA) {
            int level = getLevel(enchantedItem);
            if (WbsMath.chance(level * CHANCE_PER_LEVEL_PER_TICK)) {
                player.setVelocity(player.getVelocity().add(WbsMath.randomVector(level * 0.1)));
            }
        }
    }

    @Override
    public EnchantmentType getType() {
        return EnchantmentTypeManager.CURSE;
    }
}
