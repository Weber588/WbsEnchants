package wbs.enchants.enchantment;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.sciguymjm.uberenchant.api.utils.Rarity;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.WbsMaterials;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.utils.util.providers.NumProvider;
import wbs.utils.util.providers.generator.num.CycleGenerator;

import java.awt.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class TransferenceEnchant extends WbsEnchantment {
    private static final Map<UUID, Integer> WARPING = new HashMap<>();
    private static final int MAX_ATTEMPTS = 50;

    private static final WbsParticleGroup WIFF_EFFECT = new WbsParticleGroup()
            .addEffect(
                    new RingParticleEffect()
                        .setRadius(0.005)
                        .setRelative(true)
                        .setSpeed(0.2)
                        .setAmount(30),
                    Particle.DRAGON_BREATH
            );

    public TransferenceEnchant() {
        super("transference");
    }

    @EventHandler
    public void onUseMap(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        Player player = event.getPlayer();
        if (WARPING.containsKey(player.getUniqueId())) {
            return;
        }

        if (containsEnchantment(item)) {
            if (!(item.getItemMeta() instanceof MapMeta meta)) {
                return;
            }

            MapView mapView = meta.getMapView();
            if (mapView == null) {
                return;
            }

            World world = mapView.getWorld();
            if (world == null) {
                return;
            }

            int widthMultiplier = switch (mapView.getScale()) {
                case CLOSEST -> 1;
                case CLOSE -> 2;
                case NORMAL -> 4;
                case FAR -> 8;
                case FARTHEST -> 16;
            };

            MapView temp = Bukkit.createMap(world);
            MockCanvas canvas = new MockCanvas(temp, meta);

            mapView.getRenderers().forEach(mapRenderer -> mapRenderer.render(temp, canvas, player));

            Location location = null;
            int attempts = 0;
            int worldWidth = widthMultiplier * 128;
            do {
                attempts++;
                int xOffset = new Random().nextInt(worldWidth);
                int zOffset = new Random().nextInt(worldWidth);

                Color pixelColor = canvas.getPixelColor(xOffset / widthMultiplier, zOffset / widthMultiplier);

                if (pixelColor == null || pixelColor.getAlpha() == 0) {
                    continue;
                }

                xOffset -= worldWidth / 2;
                zOffset -= worldWidth / 2;

                location = new Location(world,
                        mapView.getCenterX() + xOffset,
                        0,
                        mapView.getCenterZ() + zOffset);

                location = world.getHighestBlockAt(location).getLocation();
            } while ((location == null || location.getY() <= world.getMinHeight()) && attempts <= MAX_ATTEMPTS);

            if (attempts >= MAX_ATTEMPTS) {
                // Nothing found, try middle location as a last resort (also allows better targeting in the end)
                location = new Location(world,
                        mapView.getCenterX(),
                        0,
                        mapView.getCenterZ());

                location = world.getHighestBlockAt(location).getLocation();
                if (location.getY() <= world.getMinHeight()) {
                    WIFF_EFFECT.play(WbsEntityUtil.getMiddleLocation(player));
                    sendActionBar("The map found nowhere to put you...", player);
                    return;
                }
            }

            int floatTicks = 5 * 20;

            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, floatTicks, 2));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, floatTicks * 2, 0));
            Location finalLocation = location.add(0, 15, 0);

            RingParticleEffect liftingEffect = new RingParticleEffect();
            liftingEffect.setRadius(1)
                    .setRotation(new NumProvider(new CycleGenerator(0, 360, 100, 0)))
                    .setAmount(3);

            int taskId = new BukkitRunnable() {
                int age = 0;

                @Override
                public void run() {
                    age++;

                    if (age >= floatTicks) {
                        cancel();
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 3 * 20 + 10, 0));

                        finalLocation.setDirection(WbsMath.getFacingVector(player));

                        player.teleport(finalLocation);
                        WARPING.remove(player.getUniqueId());
                    } else {
                        liftingEffect.buildAndPlay(Particle.DRAGON_BREATH, player.getLocation().add(0, 2, 0));
                    }
                }
            }.runTaskTimer(WbsEnchants.getInstance(), 1, 1).getTaskId();
            WARPING.put(player.getUniqueId(), taskId);
        }
    }
    
    @Override
    public @NotNull String getDescription() {
        return "A map enchantment that, when right clicked, will take you to a random point on the map!";
    }

    @Override
    public String getDisplayName() {
        return "&7Transference";
    }

    @Override
    public Rarity getRarity() {
        return Rarity.RARE;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        // Doesn't matter, overriding anyway
        return EnchantmentTarget.TOOL;
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
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack itemStack) {
        return itemStack.getType() == Material.FILLED_MAP;
    }

    private static class MockCanvas implements MapCanvas {
        private final MapView mapView;
        private final MapMeta meta;
        private MapCursorCollection mapCursorCollection = new MapCursorCollection();
        private final Table<Integer, Integer, Color> colourTable = HashBasedTable.create();

        private MockCanvas(@NotNull MapView mapView, @NotNull MapMeta meta) {
            this.meta = meta;
            this.mapView = mapView;
        }

        @NotNull
        @Override
        public MapView getMapView() {
            return mapView;
        }

        @NotNull
        @Override
        public MapCursorCollection getCursors() {
            return mapCursorCollection;
        }

        @Override
        public void setCursors(@NotNull MapCursorCollection mapCursorCollection) {
            this.mapCursorCollection = mapCursorCollection;
        }

        @Override
        public void setPixelColor(int x, int z, @Nullable Color color) {
            colourTable.put(x, z, color);
        }

        @Nullable
        @Override
        public Color getPixelColor(int x, int z) {
            return colourTable.get(x, z);
        }

        @NotNull
        @Override
        public Color getBasePixelColor(int x, int z) {
            org.bukkit.Color base = meta.getColor();
            if (base == null) {
                base = org.bukkit.Color.fromARGB(0, 0, 0, 0);
            }
            return new Color(base.getRed(), base.getGreen(), base.getGreen(), base.getAlpha());
        }

        @SuppressWarnings("deprecation")
        @Override
        public void setPixel(int x, int z, byte b) {
            colourTable.put(x, z, MapPalette.getColor(b));
        }

        @SuppressWarnings("deprecation")
        @Override
        public byte getPixel(int x, int z) {
            Color color = colourTable.get(x, z);
            if (color != null) {
                return MapPalette.matchColor(color);
            }
            return 0;
        }

        @SuppressWarnings("deprecation")
        @Override
        public byte getBasePixel(int x, int z) {
            return MapPalette.matchColor(getBasePixelColor(x, z));
        }

        @Override
        public void drawImage(int x, int z, @NotNull Image image) {
        }

        @Override
        public void drawText(int x, int z, @NotNull MapFont mapFont, @NotNull String s) {
        }
    }
}
