package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.AbstractMultiBreakEnchant;
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
    private static final String DEFAULT_DESCRIPTION = "Mines a 3x1x3 square when you mine a stone-type block, " +
            "increasing layers every level, for a maximum of 3x%max_level%x3 at level %max_level%.";

    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setAmount(1), Particle.EXPLOSION);
    private static final WbsSoundGroup SOUND = new WbsSoundGroup();

    static {
        SOUND.addSound(new WbsSound(Sound.ENTITY_GENERIC_EXPLODE));
    }

    public BlastMinerEnchant() {
        super("blast_miner", DEFAULT_DESCRIPTION);
        maxLevel = 3;
        supportedItems = ItemTypeTagKeys.PICKAXES;
        exclusiveWith = WbsEnchantsBootstrap.EXCLUSIVE_SET_MULTIMINER;
        weight = 5;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Blast Miner";
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
}
