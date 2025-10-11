package wbs.enchants.generation.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantsBootstrap;

public abstract class GenerationCondition {
    @NotNull
    protected final NamespacedKey key;

    protected boolean negated = false;
    protected Component overrideDescription;

    public GenerationCondition(@NotNull NamespacedKey generationKey, ConfigurationSection parentSection, String directory) {
        this.key = generationKey;

        ConfigurationSection section = parentSection.getConfigurationSection(generationKey.getKey());
        if (section != null) {
            this.negated = section.getBoolean("is-negated");
            this.overrideDescription = section.getRichMessage("description");
        }
    }

    protected GenerationCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        this(WbsEnchantsBootstrap.createKey(key), parentSection, directory);
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

    public Component getDescription(@NotNull TextComponent listBreak) {
        if (overrideDescription != null) {
            return overrideDescription;
        }
        return describe(listBreak);
    }
    public abstract Component describe(@NotNull TextComponent listBreak);
    public void postRegistryValidate() {}
}
