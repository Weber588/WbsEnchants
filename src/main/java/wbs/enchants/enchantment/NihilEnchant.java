package wbs.enchants.enchantment;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockDropEnchantment;
import wbs.enchants.type.EnchantmentTypeManager;

public class NihilEnchant extends WbsEnchantment implements BlockDropEnchantment {
    private static final String DEFAULT_DESCRIPTION = "Prevents all block and mob drops.";

    public NihilEnchant() {
        super("nihil", EnchantmentTypeManager.PARADOXICAL, DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_LOOT_CREATORS);
    }

    @Override
    public EventPriority getDropPriority() {
        return EventPriority.LOWEST;
    }

    @Override
    public boolean allowIncorrectTools() {
        return true;
    }

    @Override
    public void apply(BlockDropItemEvent event, MarkedLocation marked) {
        Player player = Bukkit.getPlayer(marked.playerUUID());

        ItemStack enchanted = getIfEnchanted(player);

        if (enchanted != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onKillMob(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            if (getIfEnchanted(killer) != null) {
                event.getDrops().clear();
            }
        }
    }
}
