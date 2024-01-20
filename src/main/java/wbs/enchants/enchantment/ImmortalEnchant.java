package wbs.enchants.enchantment;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.util.DamageUtils;
import wbs.enchants.util.MaterialUtils;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.string.WbsStringify;

import java.time.Duration;
import java.util.HashMap;

public class ImmortalEnchant extends WbsEnchantment {
    private static final NamespacedKey TRUE_AGE_KEY = new NamespacedKey(WbsEnchants.getInstance(), "true_age");

    private static final int ITEM_DESPAWN_AGE = 60 * 60 * 20;
    private static final int PICKUP_DELAY = 5 * 20;

    public ImmortalEnchant() {
        super("immortal");
    }

    // region Item damage/break

    // This should prevent items with Immortal from breaking, but just in case, there's also a
    // failsafe method with ItemBreakEvent (which can't be cancelled), but instead it gives 
    // item back at 0 durability.
    @EventHandler(ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();

        if (containsEnchantment(item)) {
            if (!willBreak(item, event.getDamage())) {
                return;
            }

            Player player = event.getPlayer();

            event.setCancelled(true);
            notifyImmortal(player, item);
            unequip(player, item);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemBreak(PlayerItemBreakEvent event) {
        ItemStack item = event.getBrokenItem();

        if (containsEnchantment(item)) {
            // Something went wrong with our other event! Log this and return the item as a failsafe
            WbsEnchants.getInstance().getLogger().warning("Immortal enchant failed to prevent an item break!");
            WbsEnchants.getInstance().getLogger().warning("The item should have been returned to the player," +
                    " but please report this error.");
            WbsEnchants.getInstance().getLogger().info("Item: " + item.serialize());

            giveSafely(event.getPlayer(), item);
        }
    }

    // endregion

    // region Utilities
    private void unequip(Player player, ItemStack item) {
        EquipmentSlot slot = item.getType().getEquipmentSlot();

        EntityEquipment equipment = player.getEquipment();
        Validate.notNull(equipment);

        switch (slot) {
            case FEET, LEGS, CHEST, HEAD -> {
                if (item.equals(equipment.getItem(slot))) {
                    equipment.setItem(slot, new ItemStack(Material.AIR));
                    giveSafely(player, item);
                    return;
                }
            }
        }

        // Was on a tool and it took damage from something other than an event we monitor - force drop it so they can't
        // keep using a broken Immortal tool
        player.getWorld().dropItemNaturally(player.getLocation(), item, dropped -> dropped.setPickupDelay(PICKUP_DELAY));
        item.setAmount(0);
    }

    private boolean willBreak(ItemStack item, int afterDamage) {
        if (!(item.getItemMeta() instanceof Damageable damageable)) {
            return false;
        }
        return afterDamage + damageable.getDamage() >= item.getType().getMaxDurability();
    }

    private void giveSafely(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> failed = player.getInventory().addItem(item);
        if (!failed.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    private void notifyImmortal(@NotNull Player player, @Nullable ItemStack item) {
        String displayName = "item";
        if (item != null) {
            displayName = WbsEnums.toPrettyString(item.getType());
        }

        WbsEnchants.getInstance().sendActionBar("Your " + displayName + " was saved by its "
                + getDisplayName() + "&r enchantment!", player);
    }

    // endregion

    // region Durability-causing events

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!Tag.ITEMS_TOOLS.isTagged(item.getType())) {
            return;
        }

        if (containsEnchantment(item)) {
            if (willBreak(item, 1)) {
                event.setCancelled(true);
                notifyImmortal(player, item);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmourDamage(EntityDamageEvent event) {
        if (!DamageUtils.canBeBlocked(event.getCause())) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        EntityEquipment equipment = player.getEquipment();
        Validate.notNull(equipment);

        int durabilityToTake = (int) Math.max(1, Math.floor(event.getDamage() / 4));

        for (ItemStack armourItem : equipment.getArmorContents()) {
            if (containsEnchantment(armourItem)) {
                if (willBreak(armourItem, durabilityToTake)) {
                    notifyImmortal(player, armourItem);
                    unequip(player, armourItem);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCatchFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.FISHING) {
            return;
        }
        Player player = event.getPlayer();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.FISHING_ROD) {
            item = player.getInventory().getItemInOffHand();
            if (item.getType() != Material.FISHING_ROD) {
                // How did this happen???
                return;
            }
        }

        if (containsEnchantment(item)) {
            if (willBreak(item, 1)) {
                notifyImmortal(player, item);
                event.setCancelled(true);
            }
        }
    }

    /**
     * Used to catch random events from a right click on a block that uses durability
     */
    @EventHandler(ignoreCancelled = true)
    public void useItem(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        if (containsEnchantment(item)) {
            if (!willBreak(item, 1)) {
                return;
            }

            if (Tag.ITEMS_SHOVELS.isTagged(item.getType())) {
                if (Tag.DIRT.isTagged(block.getType())) {
                    event.setCancelled(true);
                    notifyImmortal(event.getPlayer(), item);
                    return;
                }
            }

            if (Tag.ITEMS_AXES.isTagged(item.getType())) {
                if (Tag.LOGS.isTagged(block.getType()) || MaterialUtils.isAgedCopper(block.getType())) {
                    event.setCancelled(true);
                    notifyImmortal(event.getPlayer(), item);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent event) {
        ItemStack item = event.getBow();
        if (item == null) {
            return;
        }

        if (containsEnchantment(item)) {
            if (willBreak(item, 1)) {
                event.setCancelled(true);
                if (event.getEntity() instanceof Player player) {
                    notifyImmortal(player, item);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity damager)) {
            return;
        }

        EntityEquipment equipment = damager.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack held = equipment.getItemInMainHand();
        if (containsEnchantment(held)) {
            event.setCancelled(true);
            if (damager instanceof Player player) {
                notifyImmortal(player, held);
            }
            return;
        }

        for (ItemStack stack : equipment.getArmorContents()) {
            if (stack == null) {
                continue;
            }

            if (containsEnchantment(stack)) {
                ItemMeta meta = stack.getItemMeta();
                Validate.notNull(meta);
                if (meta.hasEnchant(THORNS)) {
                    event.setCancelled(true);
                    if (damager instanceof Player player) {
                        notifyImmortal(player, held);
                    }
                    return;
                }
            }
        }
    }

    // endregion

    // region Item entity events

    @EventHandler(ignoreCancelled = true)
    public void onItemDespawn(ItemDespawnEvent event) {
        Item itemEntity = event.getEntity();
        ItemStack item = itemEntity.getItemStack();

        if (containsEnchantment(item)) {
            PersistentDataContainer container = itemEntity.getPersistentDataContainer();
            Integer trueAge = container.get(TRUE_AGE_KEY, PersistentDataType.INTEGER);
            if (trueAge == null) {
                trueAge = 0;
            }

            trueAge += itemEntity.getTicksLived();

            if (trueAge < ITEM_DESPAWN_AGE) {
                event.setCancelled(true);
                itemEntity.setTicksLived(0);

                container.set(TRUE_AGE_KEY, PersistentDataType.INTEGER, trueAge);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemEntityDestroy(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item itemEntity)) {
            return;
        }

        if (containsEnchantment(itemEntity.getItemStack())) {
            if (!DamageUtils.isUnstoppable(event.getCause())) {
                event.setCancelled(true);
            }
        }
    }

    // endregion

    @Override
    public @NotNull String getDescription() {
        return "Instead of breaking, items with this enchantment will simply stop working, or unequip itself. " +
                "When dropped, the item takes " +
                WbsStringify.toString(Duration.ofSeconds(ITEM_DESPAWN_AGE / 20), true) +
                " to despawn, and cannot be destroyed except for the void.";
    }

    @Override
    public String getDisplayName() {
        return "&7Immortal";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.VERY_RARE;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment enchantment) {
        // TODO: Make this conflict with unbreaking????
        return false;
    }
}
