package wbs.enchants.generation.conditions;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import wbs.enchants.WbsEnchants;

import java.util.LinkedList;
import java.util.List;

public class BiomeCondition extends GenerationCondition {
    public static final String KEY = "biome";
    
    private List<String> matches = new LinkedList<>();
    private double minTemp = Double.MIN_VALUE;
    private double maxTemp = Double.MAX_VALUE;
    private double minHumidity = Double.MIN_VALUE;
    private double maxHumidity = Double.MAX_VALUE;

    public BiomeCondition(String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            // min-temp, max-temp, min-humidity, max-humidity, matches
            minTemp = section.getDouble("min-temp", minTemp);
            maxTemp = section.getDouble("max-temp", maxTemp);
            minHumidity = section.getDouble("min-humidity", minHumidity);
            maxHumidity = section.getDouble("max-humidity", maxHumidity);

            matches = section.getStringList("matches");
        } else {
            matches.add(parentSection.getString(key));
        }

        if (!matches.isEmpty() && !Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            WbsEnchants.getInstance().getLogger().warning("Biome condition (" + directory + ") relies on biome" +
                    " name, but WorldEdit was not found. Only vanilla (\"minecraft:\") biomes will be supported.");
        }
    }

    @Override
    public boolean test(Location location) {
        double temp = location.getBlock().getTemperature();
        double humidity = location.getBlock().getHumidity();

        World world = location.getWorld();

        if (world == null) {
            return true;
        }

        if (!matches.isEmpty()) {
            String biomeKey;
            if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
                BlockVector3 worldEditBlock = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
                BiomeType biome = BukkitAdapter.adapt(world).getBiome(worldEditBlock);
                biomeKey = biome.getId();
            } else {
                NamespacedKey key = world.getBiome(location).getKey();
                biomeKey = key.getNamespace() + ":" + key.getKey();
            }

            // Direct matches override
            boolean matchFound = matches.stream()
                    .anyMatch(regex -> biomeKey.matches(regex) || biomeKey.contains(regex));
            if (matchFound) {
                return true;
            }
        }

        return temp <= maxTemp && temp >= minTemp &&
                humidity <= maxHumidity && humidity >= minHumidity;
    }

    @Override
    public String toString() {
        return "BiomeCondition{" +
                "maxHumidity=" + maxHumidity +
                ", key=" + key +
                ", matches=" + matches +
                ", minTemp=" + minTemp +
                ", maxTemp=" + maxTemp +
                ", minHumidity=" + minHumidity +
                ", negated=" + negated +
                '}';
    }
}
