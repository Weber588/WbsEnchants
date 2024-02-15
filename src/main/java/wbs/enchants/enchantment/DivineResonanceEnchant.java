package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Sound;
import org.bukkit.block.Bell;
import org.bukkit.block.TileState;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BellRingEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.BlockEnchant;
import wbs.utils.util.WbsSound;
import wbs.utils.util.entities.selector.RadiusSelector;

public class DivineResonanceEnchant extends WbsEnchantment implements BlockEnchant {
    public DivineResonanceEnchant() {
        super("divine_resonance");
        registerBlockEvents();
    }

    @EventHandler
    public void onRing(BellRingEvent event) {
        if (event.getBlock().getState() instanceof Bell bell) {
            Integer level = getLevel(bell);
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
                }.runTaskLater(plugin, 40);
            }
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "Bells enchanted with this will apply glowing to all hostile mobs within radius, regardless of " +
                "whether or not they're pillagers, and regardless of whether there's a raid going on!";
    }

    @Override
    public String getDisplayName() {
        return "&7Divine Resonance";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.VERY_RARE;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        // Bell enchant -- Overridden below
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean canEnchant(TileState state) {
        return state instanceof Bell;
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Bell";
    }
}
