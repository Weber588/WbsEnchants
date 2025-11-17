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
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ContainerItemEnchant;
import wbs.enchants.enchantment.helper.ContainerItemWrapper;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.List;

public class SiphoningEnchant extends WbsEnchantment implements ContainerItemEnchant {
    private static final String DEFAULT_DESCRIPTION = "When you pick up an item, if the enchanted item contains any of " +
            "the same, the picked up item will go directly inside.";
    private static final WbsParticleGroup PICKUP_EFFECT = new WbsParticleGroup();

    public SiphoningEnchant() {
        super("siphoning", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_ITEM_CONTAINER);
    }

    static {
        PICKUP_EFFECT.addEffect(new NormalParticleEffect().setXYZ(0).setAmount(3), Particle.WITCH);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPickupItem(EntityPickupItemEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof Player player)) {
            return;
        }

        List<ContainerItemWrapper> autoPickupItems = getContainerItemWrappers(player);

        if (autoPickupItems.isEmpty()) {
            return;
        }

        Item dropEntity = event.getItem();
        ItemStack drop = dropEntity.getItemStack();

        if (Tag.SHULKER_BOXES.isTagged(drop.getType())) {
            return;
        }

        for (ContainerItemWrapper containerItem : autoPickupItems) {
            if (!containerItem.containsAtLeast(drop, 1)) {
                continue;
            }

            if (containerItem.canContain(drop)) {
                ItemStack failed = containerItem.addItem(drop);
                containerItem.saveToItem();

                if (failed == null || failed.isEmpty()) {
                    dropEntity.remove();
                    event.setCancelled(true);
                    break;
                } else if (failed.getAmount() != drop.getAmount()) {
                    PICKUP_EFFECT.play(dropEntity.getLocation(), WbsEntityUtil.getMiddleLocation(player));
                    dropEntity.getWorld().playSound(dropEntity.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);

                    drop.setAmount(failed.getAmount());
                    dropEntity.setItemStack(drop);
                }
            }
        }
    }
}
