package wbs.enchants.generation.conditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsKeyed;

public class VillagerProfessionCondition extends GenerationCondition {
    public static final String KEY = "villager-profession";
    private final Villager.Profession profession;

    public VillagerProfessionCondition(String key, ConfigurationSection parentSection, String directory) {
        super(key, parentSection, directory);

        String professionString;

        ConfigurationSection section = parentSection.getConfigurationSection(key);
        if (section != null) {
            professionString = section.getString("profession");
        } else {
            professionString = parentSection.getString(key);
        }

        if (professionString == null) {
            throw new InvalidConfigurationException("Specify a villager profession: " +
                    WbsKeyed.joiningPrettyStrings(Villager.Profession.class), directory);
        }

        profession = WbsKeyed.getKeyedFromString(Villager.Profession.class, professionString);

        if (profession == null) {
            throw new InvalidConfigurationException("Invalid villager profession \"" + professionString + "\". Valid options: " +
                    WbsKeyed.joiningPrettyStrings(Villager.Profession.class), directory);
        }
    }

    @Override
    public boolean test(Entity entity) {
        if (!(entity instanceof Villager villager)) {
            return false;
        }

        return villager.getProfession().equals(profession);
    }

    @Override
    public String toString() {
        return "VillagerTypeCondition{" +
                "profession=" + profession +
                ", key=" + key +
                ", negated=" + negated +
                '}';
    }

    @Override
    public Component describe(@NotNull TextComponent listBreak) {
        return Component.text("Villager profession " + profession.key());
    }
}
