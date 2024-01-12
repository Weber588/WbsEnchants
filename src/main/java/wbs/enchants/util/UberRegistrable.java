package wbs.enchants.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public interface UberRegistrable {
    void registerUberRecord();
    ConfigurationSection buildConfigurationSection(YamlConfiguration baseFile);

    void configure(ConfigurationSection section, String directory);
}
