package wbs.enchants.enchantment.curse;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.enchantment.helper.BlockDropEnchantment;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.Set;

public class CurseMidas extends BlockDropEnchantment {
    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setSpeed(0.5).setAmount(8), Particle.WAX_ON);

    public CurseMidas() {
        super("curse_midas");
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

    @Override
    public @NotNull String getDescription() {
        return "A pickaxe curse that causes mined metals to turn directly into gold!";
    }

    @Override
    public String getDisplayName() {
        return "&cCurse of Midas";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.RARE;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return Tag.ITEMS_PICKAXES.isTagged(itemStack.getType());
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @Override
    public Set<Enchantment> getDirectConflicts() {
        return Set.of(SILK_TOUCH);
    }
}
