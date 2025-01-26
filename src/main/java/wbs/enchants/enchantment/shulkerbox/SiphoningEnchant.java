package wbs.enchants.enchantment.shulkerbox;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.enchantment.helper.ShulkerBoxEnchantment;
import wbs.utils.util.WbsSound;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.HashMap;
import java.util.List;

public class SiphoningEnchant extends ShulkerBoxEnchantment {
    private static final String DEFAULT_DESCRIPTION = "When you pick up an item, if the shulker box contains any of " +
            "the same, the picked up item will go straight to the shulker box.";

    public SiphoningEnchant() {
        super("siphoning", DEFAULT_DESCRIPTION);
    }

    private static final WbsParticleGroup PICKUP_EFFECT = new WbsParticleGroup();
    private static final WbsSound PICKUP_SOUND = new WbsSound(Sound.ENTITY_ITEM_PICKUP);

    static {
        PICKUP_EFFECT.addEffect(new NormalParticleEffect().setXYZ(0).setAmount(3), Particle.WITCH);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDropItem(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player player)) {
            return;
        }

        List<ShulkerBoxWrapper> autoPickupBoxes = getEnchantedInInventory(player);

        if (autoPickupBoxes.isEmpty()) {
            return;
        }

        Item dropEntity = event.getItem();
        ItemStack drop = dropEntity.getItemStack();

        if (Tag.SHULKER_BOXES.isTagged(drop.getType())) {
            return;
        }

        for (ShulkerBoxWrapper box : autoPickupBoxes) {
            Inventory inventory = box.box().getInventory();
            if (!inventory.containsAtLeast(drop, 1)) {
                continue;
            }

            if (box.canContain(drop)) {
                HashMap<Integer, ItemStack> failed = box.box().getInventory().addItem(drop);
                box.save();

                PICKUP_EFFECT.play(dropEntity.getLocation(), WbsEntityUtil.getMiddleLocation(player));
                PICKUP_SOUND.play(dropEntity.getLocation());

                if (failed.isEmpty()) {
                    dropEntity.remove();
                    event.setCancelled(true);
                    break;
                } else {
                    ItemStack failedToAdd = failed.get(0);

                    drop.setAmount(failedToAdd.getAmount());
                    dropEntity.setItemStack(drop);
                }
            }
        }
    }
}
