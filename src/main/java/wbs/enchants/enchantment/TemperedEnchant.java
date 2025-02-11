package wbs.enchants.enchantment;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.BlockInventoryHolder;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;

public class TemperedEnchant extends WbsEnchantment implements BlockEnchant {
    private static final String DEFAULT_DESCRIPTION = "Makes the anvil break more slowly.";
    private static final double CHANCE_PER_LEVEL = 25;

    private static final WbsParticleEffect EFFECT = new NormalParticleEffect()
            .setXYZ(0.5)
            .setSpeed(0.2);

    public TemperedEnchant() {
        super("tempered", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeTagKeys.ANVIL);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAnvilDamage(AnvilDamagedEvent event) {
        if (event.getInventory().getHolder() instanceof BlockInventoryHolder holder) {
            Block block = holder.getBlock();

            Integer level = getLevel(block);
            if (level != null) {
                if (WbsMath.chance(level * CHANCE_PER_LEVEL)) {
                    event.setCancelled(true);
                    EFFECT.play(Particle.ENCHANT, block.getLocation());
                }
            }
        }
    }

    @Override
    public boolean canEnchant(Block block) {
        return Tag.ANVIL.isTagged(block.getType());
    }
}
