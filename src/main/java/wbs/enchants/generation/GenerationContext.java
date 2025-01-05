package wbs.enchants.generation;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.EnchantmentDefinition;
import wbs.enchants.generation.conditions.GenerationCondition;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.WbsMath;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public abstract class GenerationContext implements Listener {
    protected static final Random RANDOM = new Random(System.currentTimeMillis());

    protected final EnchantmentDefinition definition;
    protected final List<GenerationCondition> conditions = new LinkedList<>();
    private final double chanceToRun;

    protected final String key;

    @NotNull
    private final LevelGenerator levelGenerator;

    public GenerationContext(String key, EnchantmentDefinition definition, ConfigurationSection section, String directory) {
        this.key = key;
        this.definition = definition;
        String conditionsKey = "conditions";
        ConfigurationSection conditionsSection = section.getConfigurationSection(conditionsKey);

        directory += "/" + conditionsKey;
        if (conditionsSection != null) {
            for (String conditionKey : conditionsSection.getKeys(false)) {
                GenerationCondition condition = ConditionManager.getCondition(conditionKey, conditionsSection, directory + "/" + conditionKey);

                if (condition == null) {
                    continue;
                }

                conditions.add(condition);
            }
        }

        chanceToRun = section.getDouble("chance", getDefaultChance());

        String levelKey = "level";
        if (section.isInt(levelKey)) {
            int level = section.getInt(levelKey);
            levelGenerator = new LevelGenerator(definition, level);
        } else if (section.isString(levelKey)) {
            String modeString = Objects.requireNonNull(section.getString(levelKey));

            LevelMode mode = WbsEnums.getEnumFromString(LevelMode.class, modeString);
            if (mode == null) {
                throw new InvalidConfigurationException("Invalid mode or static level: " + modeString);
            }

            levelGenerator = new LevelGenerator(definition, mode);
        } else if (section.isConfigurationSection(levelKey)) {
            ConfigurationSection levelSection = Objects.requireNonNull(section.getConfigurationSection(levelKey));

            int staticLevel = levelSection.getInt("level", 1);
            double scalingFactor = levelSection.getDouble("scaling-factor", 2);

            String modeString = levelSection.getString("mode", "");
            LevelMode mode = WbsEnums.getEnumFromString(LevelMode.class, modeString);
            if (mode == null) {
                throw new InvalidConfigurationException("Invalid mode or static level: " + modeString);
            }

            levelGenerator = new LevelGenerator(definition, staticLevel, scalingFactor, mode);
        } else {
            levelGenerator = new LevelGenerator(definition, LevelMode.RANDOM);
        }
    }

    protected boolean meetsAllConditions(@Nullable Entity entity,
                                         @Nullable Player triggerPlayer) {
        if (entity != null) {
            return meetsAllConditions(entity, entity.getLocation().getBlock(), entity.getLocation(), triggerPlayer);
        } else {
            return meetsAllConditions(null, null, null, triggerPlayer);
        }
    }

    protected boolean meetsAllConditions(@Nullable Entity entity,
                                         @Nullable Block block,
                                         @Nullable Location location,
                                         @Nullable Player triggerPlayer)
    {
        return meetsEntityConditions(entity) &&
                meetsBlockConditions(block) &&
                meetsLocationConditions(location) &&
                meetsTriggerConditions(triggerPlayer);
    }

    @Contract("null -> true")
    protected boolean meetsEntityConditions(Entity entity) {
        if (entity == null) {
            return true;
        }
        return conditions.isEmpty() || conditions.stream()
                .anyMatch(condition -> condition.isNegated() ^ condition.test(entity));
    }

    @Contract("null -> true")
    protected boolean meetsBlockConditions(Block block) {
        if (block == null) {
            return true;
        }
        return conditions.isEmpty() || conditions.stream()
                .anyMatch(condition -> condition.isNegated() ^ condition.test(block));
    }

    @Contract("null -> true")
    protected boolean meetsLocationConditions(Location location) {
        if (location == null) {
            return true;
        }
        return conditions.isEmpty() || conditions.stream()
                .anyMatch(condition -> condition.isNegated() ^ condition.test(location));
    }

    @Contract("null -> true")
    protected boolean meetsTriggerConditions(Player player) {
        if (player == null) {
            return true;
        }
        return conditions.isEmpty() || conditions.stream()
                .anyMatch(condition -> condition.isNegated() ^ condition.testTrigger(player));
    }

    protected boolean shouldRun() {
        return WbsMath.chance(chanceToRun);
    }

    protected int generateLevel() {
        return levelGenerator.getLevel();
    }

    public abstract void writeToSection(ConfigurationSection section);

    @Contract(pure = true)
    protected abstract int getDefaultChance();

    public void createSection(ConfigurationSection parent, String basePath) {
        writeToSection(parent.createSection(basePath + "." + getKey()));
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "GenerationContext{" +
                "enchantment=" + definition.key().asString() +
                ", conditions=" + conditions.stream().map(Object::toString).collect(Collectors.joining("; ")) +
                ", chanceToRun=" + chanceToRun +
                ", key='" + key + '\'' +
                ", levelGenerator=" + levelGenerator +
                '}';
    }

    protected record LevelGenerator(EnchantmentDefinition definition, int staticLevel, double scalingFactor, LevelMode mode) {
        public LevelGenerator(EnchantmentDefinition definition, LevelMode mode) {
            this(definition, 1, 2, mode);
        }

        public LevelGenerator(EnchantmentDefinition definition, int staticLevel) {
            this(definition, staticLevel, 2, LevelMode.STATIC);
        }

        public int getLevel() {
            int maxLevel = definition.maxLevel();
            if (maxLevel == 0) {
                return staticLevel;
            }

            return switch (mode()) {
                case STATIC -> staticLevel;
                case RANDOM -> RANDOM.nextInt(maxLevel) + 1;
                case WEIGHTED_SMALL, WEIGHTED_LARGE -> {
                    int scaledMax = (int) Math.pow(scalingFactor, maxLevel);
                    int seed = RANDOM.nextInt(scaledMax);

                    int toReturn = (int) (Math.log(seed) / Math.log(scalingFactor));
                    if (mode() == LevelMode.WEIGHTED_LARGE) {
                        toReturn = maxLevel - toReturn;
                    } else {
                        toReturn += 1;
                    }
                    yield toReturn;
                }
            };
        }
    }

    protected enum LevelMode {
        /** A single static level that is always returned
         */
        STATIC,
        /** A random level between 1 and max level
         */
        RANDOM,
        /** Random level between 1 and max level, weighted towards smaller numbers (1 is scalingFactor x more likely than 2,
         * 2 is scalingFactor x more likely than 3, etc)
         */
        WEIGHTED_SMALL,
        /** Random level between 1 and max level, weighted towards larger numbers (3 is scalingFactor x more likely than 2,
         * 2 is scalingFactor x more likely than 1, etc)
         */
        WEIGHTED_LARGE
    }
}
