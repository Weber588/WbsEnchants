package wbs.enchants.generation.conditions;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class GenerationCondition {
    @NotNull
    protected final NamespacedKey key;

    protected boolean negated = false;

    public GenerationCondition(@NotNull NamespacedKey generationKey, ConfigurationSection parentSection, String directory) {
        this.key = generationKey;

        ConfigurationSection section = parentSection.getConfigurationSection(generationKey.getKey());
        if (section != null) {
            this.negated = section.getBoolean("is-negated");
        }
    }

    protected GenerationCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        this(new NamespacedKey("wbsenchants", key), parentSection, directory);
    }

    public boolean isNegated() {
        return negated;
    }

    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public boolean test(Entity entity) {
        return test(entity.getLocation());
    }
    public boolean test(Block block) {
        return test(block.getLocation());
    }
    public boolean test(Location location) {
        return true;
    }
    public boolean testTrigger(Player player) {
        return true;
    }
}
