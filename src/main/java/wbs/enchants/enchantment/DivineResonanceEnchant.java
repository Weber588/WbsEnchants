package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.ItemTypeKeys;
import org.bukkit.Sound;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.utils.util.WbsSound;
import wbs.utils.util.entities.selector.RadiusSelector;

public class DivineResonanceEnchant extends WbsEnchantment implements BlockEnchant {
    private static final String DEFAULT_DESCRIPTION = "Bells enchanted with this will apply glowing to all " +
            "hostile mobs within radius, regardless of whether or not they're raiders, " +
            "and regardless of whether there's a raid going on!";

    public DivineResonanceEnchant() {
        super("divine_resonance", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeKeys.BELL);
    }

    @Override
    public boolean canEnchant(Block block) {
        return block.getState() instanceof Bell;
    }


    @EventHandler
    public void onRing(BellRingEvent event) {
        if (event.getBlock().getState() instanceof Bell bell) {
            Integer level = getLevel(bell.getBlock());
            if (level != null) {
                new WbsSound(Sound.BLOCK_BELL_RESONATE, 1, 1).play(bell.getLocation());

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // In case bell was broken
                        if (bell.isPlaced()) {
                            PotionEffect effect = new PotionEffect(PotionEffectType.GLOWING,
                                    20 * 10,
                                    0,
                                    true,
                                    true,
                                    true);

                            new RadiusSelector<>(Monster.class)
                                    .setRange(48)
                                    .select(bell.getLocation())
                                    .forEach(effect::apply);
                        }
                    }
                }.runTaskLater(WbsEnchants.getInstance(), 40);
            }
        }
    }
}
