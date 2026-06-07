package wbs.enchants.enchantment;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.WbsEnchantsBootstrap;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class ClandestineEnchant extends WbsEnchantment {
    public static final String DESCRIPTION = "Your items are invisible to other players until they're used.";
    public static final int DEFAULT_REVEAL_TICKS = 30 * Ticks.TICKS_PER_SECOND;

    private static final Map<UUID, Integer> ENTITY_ARMOR_TASKS = new HashMap<>();
    private static final Map<UUID, Integer> ENTITY_GENERIC_TASKS = new HashMap<>();
    public static final @NotNull NamespacedKey INVISIBLE_MODEL_KEY = Material.AIR.getKey();

    private int revealTicks = DEFAULT_REVEAL_TICKS;

    public ClandestineEnchant() {
        super("clandestine", DESCRIPTION);

        getDefinition()
                .maxLevel(1)
                .supportedItems(WbsEnchantsBootstrap.ENCHANTABLE_CLANDESTINE)
                .minimumCost(30, 0)
                .maximumCost(45, 0);
    }

    @Override
    public void configure(@NotNull ConfigurationSection section, String directory) {
        super.configure(section, directory);

        revealTicks = section.getInt("reveal-ticks", DEFAULT_REVEAL_TICKS);
    }

    @EventHandler
    public void onPlayerReceiveChunk(PlayerChunkLoadEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getChunk();

        List<Player> players = List.of(player);
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof LivingEntity livingEntity) {
                hideEnchantedItems(livingEntity, players);
            }
        }
    }

    @EventHandler
    public void onEquip(EntityEquipmentChangedEvent event) {
        LivingEntity entity = event.getEntity();
        List<Player> playersSeeingEntity = getPlayersSeeingEntity(entity);

        if (playersSeeingEntity.isEmpty()) {
            return;
        }

        Map<EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> equipmentChanges = event.getEquipmentChanges();
        Map<EquipmentSlot, ItemStack> changesWithThisEnchant = new HashMap<>();

        equipmentChanges.forEach((slot, change) -> {
            if (isEnchantmentOn(change.newItem())) {
                changesWithThisEnchant.put(slot, change.newItem());
            }
        });

        if (!changesWithThisEnchant.isEmpty()) {
            WbsEnchants.getInstance().runAtEndOfTick(() -> {
                hideItems(changesWithThisEnchant, playersSeeingEntity, entity);
            });
        }
    }

    @EventHandler
    public void onTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        List<Player> playersSeeingEntity = getPlayersSeeingEntity(entity);
        if (playersSeeingEntity.isEmpty()) {
            return;
        }

        Map<EquipmentSlot, ItemStack> enchantedEquipment = getEnchantedEquipment(entity);

        if (!enchantedEquipment.isEmpty()) {
            List<EquipmentSlot> armorSlots = Arrays.stream(EquipmentSlot.values())
                    .filter(EquipmentSlot::isArmor)
                    .toList();

            revealItems(entity, playersSeeingEntity, armorSlots);

            UUID uuid = entity.getUniqueId();
            Integer currentTaskId = ENTITY_ARMOR_TASKS.get(uuid);
            if (currentTaskId != null) {
                Bukkit.getScheduler().cancelTask(currentTaskId);
            }

            int taskId = WbsEnchants.getInstance().runLater(() -> {
                hideEnchantedItems(entity, playersSeeingEntity, armorSlots);
                ENTITY_ARMOR_TASKS.remove(uuid);
            }, revealTicks);

            ENTITY_ARMOR_TASKS.put(uuid, taskId);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShoot(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof LivingEntity entity)) {
            return;
        }

        List<Player> playersSeeingEntity = getPlayersSeeingEntity(entity);
        if (playersSeeingEntity.isEmpty()) {
            return;
        }

        List<EquipmentSlot> slots = Arrays.stream(EquipmentSlot.values())
                .filter(EquipmentSlot::isHand)
                .toList();

        Map<EquipmentSlot, ItemStack> enchantedEquipment = getEnchantedEquipment(entity);

        if (!enchantedEquipment.isEmpty()) {
            revealItems(entity, playersSeeingEntity, slots);
            Map<UUID, Integer> taskMap = ENTITY_GENERIC_TASKS;

            UUID uuid = entity.getUniqueId();
            Integer currentTaskId = taskMap.get(uuid);
            if (currentTaskId != null) {
                Bukkit.getScheduler().cancelTask(currentTaskId);
            }

            int taskId = WbsEnchants.getInstance().runLater(() -> {
                hideEnchantedItems(entity, playersSeeingEntity, slots);
                taskMap.remove(uuid);
            }, revealTicks);

            taskMap.put(uuid, taskId);
        }
        
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHitWithItem(EntityDamageByEntityEvent event) {
        DamageSource damageSource = event.getDamageSource();

        LivingEntity attacker;

        Entity directEntity = damageSource.getDirectEntity();
        if (damageSource.getDirectEntity() instanceof LivingEntity check) {
            attacker = check;
        } else if (damageSource.getCausingEntity() instanceof LivingEntity check) {
            attacker = check;
        } else {
            return;
        }

        Map<UUID, Integer> taskMap;

        List<EquipmentSlot> slots;
        if (damageSource.getDamageType().equals(DamageType.THORNS)) {
            slots = Arrays.stream(EquipmentSlot.values())
                    .filter(slot -> !slot.isHand())
                    .toList();
            taskMap = ENTITY_ARMOR_TASKS;
        } else {
            taskMap = ENTITY_GENERIC_TASKS;
            if (directEntity == attacker) {
                EntityEquipment equipment = attacker.getEquipment();
                if (equipment == null) {
                    return;
                }

                slots = List.of(EquipmentSlot.HAND);
            } else if (directEntity instanceof AbstractArrow) {
                slots = Arrays.stream(EquipmentSlot.values())
                        .filter(EquipmentSlot::isHand)
                        .toList();
            } else {
                return;
            }
        }

        List<Player> playersSeeingEntity = getPlayersSeeingEntity(attacker);
        if (playersSeeingEntity.isEmpty()) {
            return;
        }

        Map<EquipmentSlot, ItemStack> enchantedEquipment = getEnchantedEquipment(attacker);

        if (!enchantedEquipment.isEmpty()) {
            revealItems(attacker, playersSeeingEntity, slots);

            UUID uuid = attacker.getUniqueId();
            Integer currentTaskId = taskMap.get(uuid);
            if (currentTaskId != null) {
                Bukkit.getScheduler().cancelTask(currentTaskId);
            }

            int taskId = WbsEnchants.getInstance().runLater(() -> {
                hideEnchantedItems(attacker, playersSeeingEntity, slots);
                taskMap.remove(uuid);
            }, revealTicks);

            taskMap.put(uuid, taskId);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        if (isEnchantmentOn(item)) {
            makeItemInvisible(item);
        }
    }

    private static @NotNull List<Player> getPlayersSeeingEntity(LivingEntity entity) {
        return entity.getLocation()
                .getChunk()
                .getPlayersSeeingChunk()
                .stream()
                .filter(player -> player.canSee(entity))
                .toList();
    }

    private void hideEnchantedItems(LivingEntity livingEntity, List<Player> players) {
        hideEnchantedItems(livingEntity, players, List.of(EquipmentSlot.values()));
    }
    private void hideEnchantedItems(LivingEntity livingEntity, List<Player> players, List<EquipmentSlot> slots) {
        Map<EquipmentSlot, ItemStack> enchantedEquipment = getEnchantedEquipment(livingEntity, slots);
        if (enchantedEquipment.isEmpty()) return;

        hideItems(enchantedEquipment, players, livingEntity);
    }

    private @NotNull Map<EquipmentSlot, ItemStack> getEnchantedEquipment(LivingEntity livingEntity) {
        return getEnchantedEquipment(livingEntity, List.of(EquipmentSlot.values()));
    }
    private @NotNull Map<EquipmentSlot, ItemStack> getEnchantedEquipment(LivingEntity livingEntity, List<EquipmentSlot> slots) {
        EntityEquipment equipment = livingEntity.getEquipment();
        if (equipment == null) {
            return new HashMap<>();
        }

        Map<EquipmentSlot, ItemStack> entityEquipment = new HashMap<>();

        for (EquipmentSlot slot : slots) {
            ItemStack item = equipment.getItem(slot);
            if (isEnchantmentOn(item)) {
                entityEquipment.put(slot, item);
            }
        }
        return entityEquipment;
    }

    private void hideItems(Map<EquipmentSlot, ItemStack> enchantedEquipment, List<Player> playersSeeingEntity, LivingEntity entity) {
        enchantedEquipment.forEach((slot, item) -> {
            if (!isEnchantmentOn(item)) {
                return;
            }

            ItemStack fakeItem = item.clone();

            makeItemInvisible(fakeItem);

            playersSeeingEntity.forEach(player -> {
                // Always send armour changes, but don't send if player == entity for others.
                if (slot.isArmor() || !player.equals(entity)) {
                    player.sendEquipmentChange(entity, slot, fakeItem);
                }
            });
        });
    }

    private static void makeItemInvisible(ItemStack toModify) {
        Equippable equippable = toModify.getData(DataComponentTypes.EQUIPPABLE);
        if (equippable != null) {
            if (equippable.assetId() != INVISIBLE_MODEL_KEY) {
                equippable = equippable.toBuilder().assetId(INVISIBLE_MODEL_KEY).build();
                toModify.setData(DataComponentTypes.EQUIPPABLE, equippable);
            }
        } else {
            toModify.setData(DataComponentTypes.ITEM_MODEL, INVISIBLE_MODEL_KEY);
        }
    }

    private void revealItems(LivingEntity entity, List<Player> playersSeeingEntity, List<EquipmentSlot> slots) {
        Map<EquipmentSlot, ItemStack> enchantedEquipment = getEnchantedEquipment(entity, slots);

        enchantedEquipment.forEach((slot, item) -> {
            if (!isEnchantmentOn(item)) {
                return;
            }
            ItemStack fakeItem = item.clone();

            makeItemModelVisible(fakeItem);
            makeArmorVisible(fakeItem);

            playersSeeingEntity.forEach(player -> {
                player.sendEquipmentChange(entity, slot, fakeItem);
            });
        });
    }

    private static void makeItemModelVisible(ItemStack toModify) {
        Key modelKey = toModify.getData(DataComponentTypes.ITEM_MODEL);
        if (modelKey == null || modelKey.equals(INVISIBLE_MODEL_KEY)) {
            Key defaultModel = toModify.getType().getDefaultData(DataComponentTypes.ITEM_MODEL);
            if (defaultModel != null) {
                toModify.setData(DataComponentTypes.ITEM_MODEL, defaultModel);
            }
        }
    }

    private static void makeArmorVisible(ItemStack toModify) {
        Equippable equippable = toModify.getData(DataComponentTypes.EQUIPPABLE);
        if (equippable != null && (INVISIBLE_MODEL_KEY.equals(equippable.assetId()) || equippable.assetId() == null)) {
            Equippable defaultEquippable = toModify.getType().getDefaultData(DataComponentTypes.EQUIPPABLE);
            if (defaultEquippable != null) {
                equippable = equippable.toBuilder().assetId(defaultEquippable.assetId()).build();
                toModify.setData(DataComponentTypes.EQUIPPABLE, equippable);
            }
        }
    }
}
