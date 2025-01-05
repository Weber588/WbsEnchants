package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsKeyed;
import wbs.utils.util.particles.CuboidParticleEffect;

import java.util.List;

public class SoulTouchEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "Allows your tool to pick up spawners!";

    public static final NamespacedKey SPAWNER_TYPE_KEY = WbsEnchantsBootstrap.createKey("spawner_type");

    public SoulTouchEnchant() {
        super("soul_touch", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.PICKAXES);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreakSpawner(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof CreatureSpawner spawner)) {
            return;
        }

        Player player = event.getPlayer();

        ItemStack enchantedItem = getIfEnchanted(player);
        if (enchantedItem != null) {
            ItemStack spawnerItem = ItemStack.of(Material.SPAWNER);

            EntityType spawnerType = spawner.getSpawnedType();

            if (spawnerType != null) {
                ItemMeta meta = spawnerItem.getItemMeta();

                String spawnerDisplayName = WbsEnums.toPrettyString(spawnerType);

                meta.lore(List.of(Component.text(spawnerDisplayName).color(NamedTextColor.YELLOW)));
                meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(SPAWNER_TYPE_KEY, PersistentDataType.STRING, spawnerType.getKey().toString());

                if (meta instanceof BlockStateMeta stateMeta) {
                    stateMeta.setBlockState(spawner);
                }

                spawnerItem.setItemMeta(meta);
            }

            Location blockLocation = block.getLocation();

            block.getWorld().dropItemNaturally(blockLocation, spawnerItem);
            CuboidParticleEffect effect = new CuboidParticleEffect();

            Location startLocation = effect.configureBlockOutline(blockLocation, blockLocation);
            effect.setScaleAmount(true);
            effect.setAmount(5);

            effect.play(Particle.SOUL, startLocation);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof CreatureSpawner spawner)) {
            return;
        }

        ItemStack item = event.getItemInHand();

        ItemMeta meta = item.getItemMeta();
        String spawnerType = meta.getPersistentDataContainer().get(SPAWNER_TYPE_KEY, PersistentDataType.STRING);
        if (spawnerType != null) {
            if (meta instanceof BlockStateMeta stateMeta) {
                stateMeta.getBlockState().copy(block.getLocation()).update();
            } else {
                spawner.setSpawnedType(WbsKeyed.getKeyedFromString(EntityType.class, spawnerType));
                spawner.update();
            }
        }
    }
}
