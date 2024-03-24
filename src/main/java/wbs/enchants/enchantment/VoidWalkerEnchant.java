package wbs.enchants.enchantment;

import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.utils.util.WbsItems;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.HashMap;
import java.util.Map;

public class VoidWalkerEnchant extends WbsEnchantment {

    private static final WbsParticleGroup EFFECT = new WbsParticleGroup()
            .addEffect(new NormalParticleEffect().setXYZ(0.5).setY(1.5), Particle.SPELL_WITCH)
            .addEffect(new NormalParticleEffect().setXYZ(0.05).setY(0.5), Particle.SMOKE_LARGE);


    public VoidWalkerEnchant() {
        super("void_walker");
    }

    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.VOID) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) {
            return;
        }

        EntityEquipment equipment = living.getEquipment();
        if (equipment == null) {
            return;
        }

        ItemStack boots = equipment.getBoots();
        if (boots != null && containsEnchantment(boots)) {
            int maxHeight = living.getWorld().getMaxHeight();
            Location teleportLoc = living.getLocation();
            teleportLoc.setY(maxHeight + living.getHeight());

            EFFECT.play(living.getLocation());
            living.teleport(teleportLoc);
            EFFECT.play(living.getLocation());

            if (living instanceof Player player) {
                int eventDamage = (int) Math.max(Math.ceil(event.getDamage()), 1);

                WbsItems.damageItem(player, boots, eventDamage, EquipmentSlot.FEET);
            }
        }
    }

    @Override
    public String getDisplayName() {
        return "&7Void Walker";
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return EnchantmentRarity.VERY_RARE;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_FEET;
    }

    @Override
    public boolean isTreasure() {
        return true;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public @NotNull String getDescription() {
        return "When you fall into the void, you're immediately teleported to the top of the world - get that bucket " +
                "clutch ready!";
    }

    @Override
    public @NotNull Map<NamespacedKey, Double> getLootKeyChances() {
        Map<NamespacedKey, Double> tableChances = new HashMap<>();

        // TODO: Make these configurable in a config
        tableChances.put(NamespacedKey.fromString("stellarity:void_fishing/treasure"), 80.0);

        return tableChances;
    }
}
