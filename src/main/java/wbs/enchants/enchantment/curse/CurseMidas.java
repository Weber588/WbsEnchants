package wbs.enchants.enchantment.curse;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.BlockDropEnchantment;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.List;

public class CurseMidas extends BlockDropEnchantment {
    private static final String DEFAULT_DESCRIPTION = "A pickaxe curse that causes mined metals to turn " +
            "directly into gold!";

    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setSpeed(0.5).setAmount(8), Particle.WAX_ON);

    public CurseMidas() {
        super("curse/midas", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.PICKAXES;
        exclusiveWith = WbsEnchantsBootstrap.EXCLUSIVE_SET_MIDAS;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Curse of Midas";
    }

    @Override
    protected void apply(BlockDropItemEvent event, MarkedLocation marked) {
        // TODO: Make this configurable
        for (Item item : event.getItems()) {
            Material current = item.getItemStack().getType();
            Material changeTo = switch (current) {
                case RAW_IRON, RAW_COPPER -> Material.RAW_GOLD;
                case RAW_IRON_BLOCK, RAW_COPPER_BLOCK -> Material.RAW_GOLD_BLOCK;
                case IRON_BLOCK, COPPER_BLOCK, WEATHERED_COPPER, EXPOSED_COPPER, OXIDIZED_COPPER -> Material.GOLD_BLOCK;
                case IRON_ORE, COPPER_ORE -> Material.GOLD_ORE;
                case DEEPSLATE_IRON_ORE, DEEPSLATE_COPPER_ORE -> Material.DEEPSLATE_GOLD_ORE;
                default -> current;
            };

            if (current != changeTo) {
                item.getItemStack().setType(changeTo);

                EFFECT.play(item.getLocation());
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull List<TagKey<Enchantment>> addToTags() {
        return List.of(
                EnchantmentTagKeys.CURSE
        );
    }
}
