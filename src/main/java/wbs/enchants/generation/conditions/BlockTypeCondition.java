package wbs.enchants.generation.conditions;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchants;
import wbs.utils.util.WbsEnums;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class BlockTypeCondition extends GenerationCondition {
    public static final String KEY = "block-type";

    private final List<Material> exactMatches = new LinkedList<>();
    private final List<Tag<Material>> tagMatches = new LinkedList<>();

    public BlockTypeCondition(@NotNull String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        List<String> matches;
        if (parentSection.isConfigurationSection(key)) {
            ConfigurationSection section = parentSection.getConfigurationSection(key);

            matches = Objects.requireNonNull(section).getStringList("matches");
            directory += "/matches";
        } else {
            matches = parentSection.getStringList(key);
        }

        for (String check : matches) {
            Material found = WbsEnums.getEnumFromString(Material.class, check);
            if (found != null) {
                exactMatches.add(found);
            }

            NamespacedKey tagKey = NamespacedKey.fromString(check);
            if (tagKey == null) {
                tagKey = NamespacedKey.minecraft(check);
            }

            Tag<Material> tag = Bukkit.getTag("blocks", tagKey, Material.class);

            if (tag != null) {
                tagMatches.add(tag);
            }

            if (tag == null && found == null) {
                WbsEnchants.getInstance().settings.logError("Material/block tag not found: \"" + check + "\"", directory);
            }
        }
    }

    @Override
    public boolean test(Location location) {
        Material type = location.getBlock().getType();

        if (exactMatches.stream().anyMatch(exactMatches::contains)) {
            return true;
        }

        for (Tag<Material> tag : tagMatches) {
            if (tag.isTagged(type)) {
                return true;
            }
        }

        return false;
    }
}
