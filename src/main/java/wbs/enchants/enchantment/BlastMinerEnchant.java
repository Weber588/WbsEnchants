package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.EnchantsSettings;
import wbs.enchants.util.BlockChanger;
import wbs.enchants.util.BlockQueryUtils;
import wbs.utils.util.WbsSound;
import wbs.utils.util.WbsSoundGroup;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class BlastMinerEnchant extends AbstractMultiBreakEnchant {
    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setAmount(1), Particle.EXPLOSION_LARGE);
    private static final WbsSoundGroup SOUND = new WbsSoundGroup();

    static {
        SOUND.addSound(new WbsSound(Sound.ENTITY_GENERIC_EXPLODE));
    }

    public BlastMinerEnchant() {
        super("blast_miner");
    }


    // Have to catch it in this class because EventHandler reflection is used on the object, not the parents :(
    @EventHandler
    @Override
    protected void catchEvent(BlockBreakEvent event) {
        onBreakBlock(event);
    }

    @Override
    protected boolean canBreak(Block block) {
        // TODO: Create a list of materials configurable with direct names or namespaced tags
        Material type = block.getType();
        if (Tag.BASE_STONE_OVERWORLD.isTagged(type)) {
            return true;
        }
        if (Tag.BASE_STONE_NETHER.isTagged(type)) {
            return true;
        }

        return switch (type) {
            case END_STONE, COBBLESTONE, MOSSY_COBBLESTONE -> true;
            default -> false;
        };
    }

    @Override
    protected void handleBreak(@NotNull BlockBreakEvent event, @NotNull Block broken, @NotNull Player player, @NotNull ItemStack item, int level) {
        Predicate<Block> matching = this::canBreak;
        BlockFace face = getTargetBlockFace(player);

        if (face == null) {
            return;
        }

        final List<Block> blocksToBreak = new LinkedList<>();

        Block current = broken;
        for (int i = 0; i < level && matching.test(current); i++) {
            List<Block> toAdd = BlockQueryUtils.getSquareMatching(current, 1, face, matching);
            blocksToBreak.addAll(toAdd);
            blocksToBreak.add(current);
            current = current.getRelative(face.getOppositeFace());
        }

        blocksToBreak.remove(broken);

        BlockChanger.prepare(blocksToBreak)
                .setDelayTicks(1)
                .setToUpdatePerChunk(9)
                .setMatching(matching)
                .breakBlocks(player);

        EFFECT.play(broken.getLocation());
        SOUND.play(broken.getLocation());
    }

    @Override
    public String getDisplayName() {
        return "&7Blast Miner";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        return enchantment == EnchantsSettings.VEIN_MINER;
    }

    @Override
    public @NotNull String getDescription() {
        return "Mines a 3x1x3 square when you mine a stone-type block, increasing layers every level, for a maximum of " +
                "3x" + getMaxLevel() + "x3 at level " + getMaxLevel() + ".";
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Pickaxe";
    }
}
