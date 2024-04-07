package wbs.enchants.enchantment.curse;

import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;

public class CurseInsomnia extends WbsEnchantment {
    public CurseInsomnia() {
        super("curse_insomnia");
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        // Just check if there's ANY armour with this enchant on it, don't care about details
        if (getHighestEnchantedArmour(player) != null) {
            event.setCancelled(true);
            sendActionBar("&wThe " + getDisplayName() + "&w prevents your sleep...", player);
        }
    }

    @Override
    public @NotNull String getDescription() {
        return "An armour curse that prevents the player from sleeping while worn.";
    }

    @Override
    public String getDisplayName() {
        return "&cCurse of Insomnia";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.COMMON;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return true;
    }
}
