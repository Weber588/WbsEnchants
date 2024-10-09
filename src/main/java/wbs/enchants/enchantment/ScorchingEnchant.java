package wbs.enchants.enchantment;

import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;

import java.util.*;

public class ScorchingEnchant extends WbsEnchantment {
    private static final Map<Location, ScorchedBlock> SCORCHED = new HashMap<>();
    private static int timerId = -1;

    private static final WbsParticleGroup SMELT_EFFECT = new WbsParticleGroup().addEffect(
            new NormalParticleEffect().setXYZ(0.15).setAmount(10), Particle.SMALL_FLAME
    );

    private static void createScorch(Player player, Block scorchedBlock, Location location) {
        ScorchedBlock entanglement = new ScorchedBlock(player.getUniqueId(), scorchedBlock, System.currentTimeMillis());
        SCORCHED.put(location, entanglement);

        startDescorchTimer();
    }
    private static void startDescorchTimer() {
        if (timerId != -1) {
            return;
        }

        timerId = new BukkitRunnable() {
            @Override
            public void run() {
                List<Location> toRemove = new LinkedList<>();
                for (Location location : SCORCHED.keySet()) {
                    ScorchedBlock scorched = SCORCHED.get(location);
                    if (scorched.createdTimestamp + 1000 < System.currentTimeMillis()) {
                        toRemove.add(location);
                    }
                }

                toRemove.forEach(SCORCHED::remove);

                if (SCORCHED.isEmpty()) {
                    cancel();
                    timerId = -1;
                }
            }
        }.runTaskTimer(WbsEnchants.getInstance(), 20, 20).getTaskId();
    }

    private static final String DEFAULT_DESCRIPTION = "Smelts blocks you mine - simple!";

    public ScorchingEnchant() {
        super("scorching", DEFAULT_DESCRIPTION);

        supportedItems = ItemTypeTagKeys.ENCHANTABLE_MINING;
        exclusiveWith = EnchantmentTagKeys.EXCLUSIVE_SET_MINING;
        weight = 1;
    }

    @Override
    public String getDefaultDisplayName() {
        return "Scorching";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block broken = event.getBlock();
        Player player = event.getPlayer();

        ScorchedBlock scorched = SCORCHED.get(broken.getLocation());
        if (scorched != null) {
            return;
        }

        BlockState state = broken.getState();
        if (state instanceof Container) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!broken.isPreferredTool(item)) {
            return;
        }

        if (isEnchantmentOn(item)) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                return;
            }

            createScorch(player, broken, broken.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        ScorchedBlock scorched = SCORCHED.get(event.getBlock().getLocation());
        if (scorched == null) {
            return;
        }

        Block entangledBlock = scorched.scorchedBlock;

        for (Item item : event.getItems()) {
            ItemStack stack = item.getItemStack();

            ItemStack result = null;
            int escape = 0;
            for (Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
                Recipe recipe;
                try {
                    escape++;
                    recipe = it.next();
                } catch (IllegalArgumentException ex) {
                    if (escape < 100) {
                        throw ex;
                    }
                    continue;
                }

                if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                    RecipeChoice inputChoice = furnaceRecipe.getInputChoice();
                    if (inputChoice.test(stack)) {
                        result = furnaceRecipe.getResult();
                        result.setAmount(result.getAmount() * stack.getAmount());
                        break;
                    }
                }
            }

            if (result != null) {
                item.setItemStack(result);

                SMELT_EFFECT.play(item.getLocation());
                SMELT_EFFECT.play(entangledBlock.getLocation().add(0.5, 1, 0.5));
            }
        }
    }

    private record ScorchedBlock(UUID playerUUID, Block scorchedBlock, Long createdTimestamp) {}
}
