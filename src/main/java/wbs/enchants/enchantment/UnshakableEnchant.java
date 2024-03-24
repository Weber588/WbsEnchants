package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.enchants.events.LeashEvents;
import wbs.utils.util.persistent.WbsPersistentDataType;

import java.util.List;

public class UnshakableEnchant extends WbsEnchantment {

    private static final List<EntityType> UNLEASHABLE_TYPES =
            List.of(EntityType.BAT,
                    EntityType.PLAYER,
                    EntityType.ENDER_DRAGON,
                    EntityType.WITHER
            );

    public UnshakableEnchant() {
        super("unshakable");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack item = equipment.getItem(event.getHand());

        Entity entity = event.getRightClicked();

        if (containsEnchantment(item)) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }

            if (livingEntity.isLeashed()) {
                return;
            }

            EntityType type = entity.getType();
            if (UNLEASHABLE_TYPES.contains(type)) {
                if (type == EntityType.PLAYER) {
                    sendActionBar("&wPlayers are too smart to be leashed...", player);
                } else if (type == EntityType.BAT) {
                    sendMessage(BAT_RANT, player);
                } else {
                    sendActionBar("&wThat mob is far too strong to be contained...", player);
                }
                return;
            }

            // Bug in bukkit with order events are run, that cause it to override leash holder after updating
            // in the interaction event. Run next tick instead.
            WbsEnchants.getInstance().runSync(() -> {
                boolean success = livingEntity.setLeashHolder(player);
                if (!success) {
                    return;
                }

                PlayerLeashEntityEvent playerEvent = new PlayerLeashEntityEvent(entity, player, player, event.getHand());
                Bukkit.getPluginManager().callEvent(playerEvent);

                if (!playerEvent.isCancelled()) {
                    ItemStack clone = item.clone();
                    clone.setAmount(1);
                    player.getInventory().removeItem(clone);
                } else {
                    livingEntity.setLeashHolder(null);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onUnleash(EntityUnleashEvent event) {
        if (event.getReason() != EntityUnleashEvent.UnleashReason.DISTANCE) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        Entity leashHolder = entity.getLeashHolder();

        // Clear the item tag so it doesn't drop
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (container.has(LeashEvents.LEASH_ITEM_KEY)) {
            ItemStack item = container.get(LeashEvents.LEASH_ITEM_KEY, WbsPersistentDataType.ITEM);

            if (item != null && containsEnchantment(item)) {
                entity.setLeashHolder(null);
                container.remove(LeashEvents.LEASH_ITEM_KEY);
                WbsEnchants.getInstance().runSync(() -> {
                    entity.teleport(leashHolder);
                    entity.setLeashHolder(leashHolder);
                    container.set(LeashEvents.LEASH_ITEM_KEY, WbsPersistentDataType.ITEM, item);
                });
            }
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "A lead enchantment that allows you to leash hostile mobs!";
    }

    @Override
    public String getDisplayName() {
        return "&7Unshakeable";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.UNCOMMON;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        // Doesn't matter - overriding with canEnchant
        return EnchantmentTarget.ALL;
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
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType() == Material.LEAD;
    }

    @Override
    public @NotNull String getTargetDescription() {
        return "Leash";
    }

    private static final String BAT_RANT = "&wBats are, for some ungodly reason, " +
            "completely unleashable. It's one of a small few mobs that you can't force a leash on. " +
            "Why? I have no idea. They just can't be. I don't know why. I truly have no idea. " +
            "And instead of doing something crazy like implementing leash physics myself, I was just " +
            "like, \"Oh, I'll just have an error message about why bats can't be leashed!\" But like... " +
            "How does that make sense. You can leash just about anything with this. " +
            "Like, elder guardians? No worries. Phantoms? Easy! But BATS can't be leashed??? " +
            "How on earth can I make that make sense? You can leash silverfish and endermites and " +
            "endermen and villagers and iron golems and VEX and SHULKERS with this. " +
            "But heavens forbid that a &lBAT&w be leashed??????????? " +
            "Anyway sorry about your chat history lol, I needed to vent. Sorry about not being able " +
            "to take that bat home with you. You should probably kill it or something to take out " +
            "your frustration.";
}
