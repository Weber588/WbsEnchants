package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.TickableEnchant;
import wbs.utils.util.entities.WbsEntityUtil;

public class AbyssMinerEnchant extends WbsEnchantment implements TickableEnchant {
    private static final String DEFAULT_DESCRIPTION = "Grants increasing levels of Haste the further away from the surface you are.";
    private static final int MAX_HASTE_LEVEL = 3;

    public AbyssMinerEnchant() {
        super("abyss_miner", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(ItemTypeTagKeys.PICKAXES)
                .activeSlots(EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public int getTickFrequency() {
        return 40;
    }

    @Override
    public void onTickEquipped(LivingEntity owner) {
        if (!(owner instanceof Player player)) {
            return;
        }

        World world = owner.getWorld();
        Location location = owner.getLocation();
        int blockY = location.getBlockY();
        int highestBlockY = world.getHighestBlockYAt(location);
        int worldMinHeight = world.getMinHeight();

        int heightAboveWorldFloor = blockY - worldMinHeight;
        int undergroundHeight = highestBlockY - worldMinHeight;
        double abyssTotalHeight = undergroundHeight * 0.75;

        if (!WbsEntityUtil.canSeeSky(owner) && blockY < worldMinHeight + abyssTotalHeight) {
            PotionEffect effect = getLevelledHaste(heightAboveWorldFloor, abyssTotalHeight);

            player.addPotionEffect(effect);
        }
    }

    private @NotNull PotionEffect getLevelledHaste(int heightAboveWorldFloor, double abyssMaxBlockY) {
        double fractionBetween = heightAboveWorldFloor / abyssMaxBlockY;
        int hasteLevel = (int) Math.min(Math.round(MAX_HASTE_LEVEL * (1 - fractionBetween)), MAX_HASTE_LEVEL);

        // If under an ocean (max y height 64):
        // haste 4 < -50

        return new PotionEffect(PotionEffectType.HASTE,
                getTickFrequency() + 20,
                hasteLevel,
                true,
                false,
                true);
    }
}
