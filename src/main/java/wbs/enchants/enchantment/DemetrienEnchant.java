package wbs.enchants.enchantment;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;

import java.util.Set;

public class DemetrienEnchant extends WbsEnchantment {
    private static final Set<Material> TILLABLE = Set.of(
            Material.DIRT,
            Material.DIRT_PATH,
            Material.GRASS_BLOCK
    );

    public static final String DESCRIPTION = "Farmland tilled by items with this enchantment " +
            "will never dry out.";

    public DemetrienEnchant() {
        super("demetrien", DESCRIPTION);
    }

    @EventHandler
    public void onTill(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null && TILLABLE.contains(block.getType())) {
            Player player = event.getPlayer();
            ItemStack held = getIfEnchanted(player);

            if (held != null) {
                // Add block to chunk to detect in future
            }
        }
    }
}
