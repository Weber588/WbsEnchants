package wbs.enchants.enchantment;

import io.papermc.paper.entity.PaperShearable;
import io.papermc.paper.entity.Shearable;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import net.kyori.adventure.util.Ticks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import org.bukkit.NamespacedKey;
import org.bukkit.block.TileState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftDispenser;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ShearingEnchant;
import wbs.utils.util.entities.selector.RadiusSelector;

import java.util.List;
import java.util.Objects;

public class IndustriousEnchant extends WbsEnchantment implements ShearingEnchant {
    private static final NamespacedKey LAST_USED_KEY = WbsEnchantsBootstrap.createKey("last_used");

    private static final @NotNull String DEFAULT_DESCRIPTION = "Shears multiple sheep at a time.";

    private static final int RADIUS_PER_LEVEL = 1;
    private static final int SHEEP_PER_LEVEL = 5;
    private static final int COOLDOWN_TICKS = 20;

    private int radiusPerLevel = RADIUS_PER_LEVEL;
    private int sheepPerLevel = SHEEP_PER_LEVEL;

    public IndustriousEnchant() {
        super("industrious", DEFAULT_DESCRIPTION);

        getDefinition()
                .maxLevel(3)
                .supportedItems(ItemTypeKeys.SHEARS)
                .minimumCost(15, 9)
                .maximumCost(65, 9);
    }

    @Override
    public void configure(@NotNull ConfigurationSection section, String directory) {
        super.configure(section, directory);

        radiusPerLevel = section.getInt("radius-per-level", radiusPerLevel);
        sheepPerLevel = section.getInt("sheep-per-level", sheepPerLevel);
    }

    @Override
    public void onShearSheep(ShearEntityEvent event) {
        Entity shearedEntity = event.getShearedEntity();

        ItemStack tool = event.getTool();

        PlayerShearEntityEvent playerEvent = event.getPlayerEvent();
        BlockShearEntityEvent blockEvent = event.getBlockEvent();

        CraftItemStack craftTool = (CraftItemStack) tool;
        net.minecraft.world.item.ItemStack nmsTool = craftTool.handle;

        PersistentDataContainer container = null;
        ServerLevel serverLevel = null;
        if (playerEvent != null) {
            Player player = playerEvent.getPlayer();
            serverLevel = ((CraftWorld) player.getWorld()).getHandle();
            container = player.getPersistentDataContainer();
        }
        TileState state = null;
        if (blockEvent != null) {
            state = (TileState) blockEvent.getBlock().getState();
            serverLevel = ((CraftWorld) blockEvent.getBlock().getWorld()).getHandle();
            container = state.getPersistentDataContainer();
        }

        Objects.requireNonNull(serverLevel);
        Objects.requireNonNull(container);

        Long lastUsedMilli = container.get(LAST_USED_KEY, PersistentDataType.LONG);
        if (lastUsedMilli != null && lastUsedMilli >= System.currentTimeMillis() - COOLDOWN_TICKS * Ticks.SINGLE_TICK_DURATION_MS) {
            return;
        }

        container.set(LAST_USED_KEY, PersistentDataType.LONG, System.currentTimeMillis());
        if (state != null) {
            state.update();
        }

        int level = getLevel(tool);

        List<Shearable> toShear = new RadiusSelector<>(Shearable.class)
                .setRange(1 + level * radiusPerLevel)
                .setPredicate(entity ->
                        entity.getType().equals(shearedEntity.getType()) &&
                                !entity.equals(shearedEntity) &&
                                entity.readyToBeSheared()
                ).select(shearedEntity);

        for (Shearable entity : toShear) {
            net.minecraft.world.entity.Shearable shearable = ((PaperShearable) entity).getHandle();
            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();

            List<net.minecraft.world.item.ItemStack> defaultDrops = shearable.generateDefaultDrops(serverLevel, nmsTool);

            List<ItemStack> drops = null;
            if (playerEvent != null && playerEvent.getPlayer() instanceof CraftPlayer craftPlayer) {
                InteractionHand interactionHand = CraftEquipmentSlot.getHand(playerEvent.getHand());
                PlayerShearEntityEvent pse = CraftEventFactory.handlePlayerShearEntityEvent(craftPlayer.getHandle(), nmsEntity, nmsTool, interactionHand, defaultDrops);

                drops = pse.getDrops();
            }
            if (blockEvent != null && blockEvent.getBlock().getState() instanceof CraftDispenser dispenser) {
                BlockShearEntityEvent bse = CraftEventFactory.callBlockShearEntityEvent(nmsEntity, dispenser.getBlock(), craftTool, defaultDrops);

                drops = bse.getDrops();
            }

            Objects.requireNonNull(drops);

            shearable.shear(serverLevel, SoundSource.PLAYERS, nmsTool, org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(drops));
        }
    }
}
