package wbs.enchants.enchantment.helper;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.util.EventUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public interface ShearingEnchant extends EnchantInterface, AutoRegistrableEnchant {

    default void registerShearEvents() {
        EventUtils.register(PlayerShearEntityEvent.class, this::onShearSheep, EventPriority.NORMAL, true);
        EventUtils.register(BlockShearEntityEvent.class, this::onShearSheep, EventPriority.NORMAL, true);
    }

    default void onShearSheep(PlayerShearEntityEvent event) {
        if (!getThisEnchantment().isEnchantmentOn(event.getItem())) {
            return;
        }
        onShearSheep(new ShearEntityEvent(event));
    }

    default void onShearSheep(BlockShearEntityEvent event) {
        if (!getThisEnchantment().isEnchantmentOn(event.getTool())) {
            return;
        }
        onShearSheep(new ShearEntityEvent(event));
    }

    void onShearSheep(ShearEntityEvent event);

    class ShearEntityEvent {
        private final Consumer<List<ItemStack>> setDrops;
        private final List<ItemStack> drops;
        private final ItemStack tool;
        private final Entity shearedEntity;
        @Nullable
        private final PlayerShearEntityEvent playerEvent;
        @Nullable
        private final BlockShearEntityEvent blockEvent;

        public ShearEntityEvent(PlayerShearEntityEvent event) {
            setDrops = event::setDrops;
            drops = new LinkedList<>(event.getDrops());
            tool = event.getItem();
            shearedEntity = event.getEntity();
            playerEvent = event;
            blockEvent = null;
        }

        public ShearEntityEvent(BlockShearEntityEvent event) {
            setDrops = event::setDrops;
            drops = new LinkedList<>(event.getDrops());
            tool = event.getTool();
            shearedEntity = event.getEntity();
            playerEvent = null;
            blockEvent = event;
        }

        public void setDrops(List<ItemStack> drops) {
            this.setDrops.accept(drops);
        }

        public List<ItemStack> getDrops() {
            return drops;
        }

        public ItemStack getTool() {
            return tool;
        }

        public Entity getShearedEntity() {
            return shearedEntity;
        }

        public @Nullable PlayerShearEntityEvent getPlayerEvent() {
            return playerEvent;
        }

        public @Nullable BlockShearEntityEvent getBlockEvent() {
            return blockEvent;
        }
    }

    enum ShearSource {
        PLAYER,
        BLOCK
    }
}
