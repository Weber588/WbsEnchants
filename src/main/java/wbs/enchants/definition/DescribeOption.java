package wbs.enchants.definition;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.enchants.EnchantManager;
import wbs.enchants.WbsEnchantRegistries;
import wbs.enchants.WbsEnchantsBootstrap;
import wbs.enchants.enchantment.helper.ConflictEnchantment;
import wbs.enchants.util.EnchantUtils;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.string.RomanNumerals;
import wbs.utils.util.string.WbsStrings;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
@NullMarked
public class DescribeOption implements Keyed {
    public static final Style BASE_STYLE = Style.style(NamedTextColor.AQUA);
    public static final Style HIGHLIGHT = Style.style(NamedTextColor.GRAY);
    public static final TextComponent LINE_START = Component.text("\n - ").style(HIGHLIGHT);

    public static final DescribeOption TYPE = new DescribeOption("type", DescribeOption::type);
    public static final DescribeOption MAX_LEVEL = new DescribeOption("max_level", DescribeOption::maxLevel);
    public static final DescribeOption TARGET = new DescribeOption("target", DescribeOption::target);
    public static final DescribeOption ACTIVE_SLOTS = new DescribeOption("active_slots", DescribeOption::activeSlots);
    public static final DescribeOption DESCRIPTION = new DescribeOption("description", DescribeOption::description);
    public static final DescribeOption WEIGHT = new DescribeOption("weight", DescribeOption::weight);
    public static final DescribeOption ANVIL_COST = new DescribeOption("anvil_cost", DescribeOption::anvilCost);
    public static final DescribeOption COSTS = new DescribeOption("costs", DescribeOption::costs);
    public static final DescribeOption TAGS = new DescribeOption("tags", DescribeOption::tags);
    public static final DescribeOption GENERATION = new DescribeOption("generation", DescribeOption::generation);
    public static final DescribeOption CONFLICTS = new DescribeOption("conflicts", DescribeOption::conflicts);

    private final NamespacedKey key;
    private final Function<EnchantmentDefinition, @Nullable Component> describeFunction;

    private DescribeOption(@Subst("key") String key, Function<EnchantmentDefinition, @Nullable Component> describeFunction) {
        this(WbsEnchantsBootstrap.createKey(key), describeFunction);
    }
    public DescribeOption(NamespacedKey key, Function<EnchantmentDefinition, @Nullable Component> describeFunction) {
        this.key = key;
        this.describeFunction = describeFunction;

        WbsEnchantRegistries.DESCRIBE_OPTIONS.register(this);
    }

    private static Component type(EnchantmentDefinition definition) {
        return Component.text("Type: ").append(definition.getType().getNameComponent());
    }

    private static Component maxLevel(EnchantmentDefinition definition) {
        return Component.text("Maximum level: ").append(
                Component.text(RomanNumerals.toRoman(definition.maxLevel()) + " (" + definition.maxLevel() + ")")
                        .style(HIGHLIGHT)
        );
    }

    private static Component target(EnchantmentDefinition definition) {
        return Component.text("Target: ").append(
                definition.getTargetDescription(LINE_START).applyFallbackStyle(HIGHLIGHT)
        );
    }

    private static Component description(EnchantmentDefinition definition) {
        return Component.text("Description: ").append(
                definition.description().applyFallbackStyle(HIGHLIGHT));
    }

    @Nullable
    private static Component generation(EnchantmentDefinition definition) {
        if (!definition.canGenerate()) {
            return null;
        }
        return Component.text("Generation:")
                .append(
                        definition.getGenerationInfo(LINE_START)
                ).style(BASE_STYLE);
    }

    @Nullable
    private static Component conflicts(EnchantmentDefinition definition) {
        List<Enchantment> conflicts = EnchantUtils.getConflictsWith(definition.getEnchantment());

        // Don't show enchants that only exist to conflict (typically curses)
        conflicts.removeIf(check -> EnchantUtils.getAsCustom(check) instanceof ConflictEnchantment);
        conflicts.removeIf(check -> check.key().equals(definition.key()));

        if (!conflicts.isEmpty()) {
            Component conflictsComponent = Component.text("Conflicts with: ");

            // If this is a conflict enchantment, show that description instead.
            if (EnchantManager.getCustomFromKey(definition.key()) instanceof ConflictEnchantment conflictEnchant) {
                conflictsComponent = conflictsComponent.append(Component.text(conflictEnchant.getConflictsDescription()));
            } else if (!conflicts.isEmpty()) {
                conflicts.sort(Comparator.comparing(org.bukkit.Keyed::getKey));

                for (Enchantment conflict : conflicts) {
                    EnchantmentDefinition conflictDef = EnchantManager.getFrom(conflict);

                    Component conflictComponent;
                    if (conflictDef == null) {
                        conflictComponent = EnchantUtils.getDisplayName(conflict);
                    } else {
                        conflictComponent = conflictDef.interactiveDisplay();
                    }

                    conflictsComponent = conflictsComponent.append(LINE_START)
                            .append(conflictComponent);
                }

            }

            return conflictsComponent;
        }

        return null;
    }

    private static final Set<@Nullable TagKey<Enchantment>> IGNORED_TAGS = Set.of(
            WbsEnchantsBootstrap.CUSTOM,
            WbsEnchantsBootstrap.VANILLA
    );

    private static @Nullable Component tags(EnchantmentDefinition definition) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);

        List<Tag<Enchantment>> tags = new LinkedList<>();
        for (Tag<Enchantment> tag : registry.getTags()) {
            if (tag.contains(definition.getTypedKey())) {
                if (!IGNORED_TAGS.contains(tag.tagKey())) {
                    tags.add(tag);
                }
            }
        }

        if (tags.isEmpty()) {
            return null;
        }

        Map<TypedKey<Enchantment>, Component> enchantmentDisplays = new HashMap<>();

        registry.stream()
                .forEach(ench -> enchantmentDisplays.put(
                        TypedKey.create(RegistryKey.ENCHANTMENT, ench.key()),
                        EnchantUtils.getDisplayName(ench).applyFallbackStyle(HIGHLIGHT)
                    )
                );

        Component component = Component.text("In tags: ");

        List<Component> tagComponents = new LinkedList<>();

        for (Tag<Enchantment> tag : tags) {
            Component tagEnchants = Component.join(
                    JoinConfiguration.builder().separator(Component.text(", ").style(BASE_STYLE)).build(),
                    tag.values().stream()
                            .distinct()
                            .sorted()
                            .map(enchantmentDisplays::get)
                            .toList()
            );

            TextComponent tagComponent = Component.text("#" + tag.tagKey().key().asMinimalString()).style(HIGHLIGHT)
                    .hoverEvent(HoverEvent.showText(tagEnchants));

            tagComponents.add(tagComponent);
        }

        return component.append(
                Component.join(
                        JoinConfiguration.builder().separator(Component.text(", ").style(BASE_STYLE)).build(),
                        tagComponents
                )
        );
    }


    private static Component weight(EnchantmentDefinition definition) {
        return Component.text("Weight: ").append(Component.text(definition.weight()).style(HIGHLIGHT));
    }

    private static Component activeSlots(EnchantmentDefinition definition) {
        List<TextComponent> slotComponents = definition.activeSlots()
                .stream()
                .map(group -> {
                    String asString = WbsStrings.capitalizeAll(group.toString());
                    return Component.text(asString).style(HIGHLIGHT).hoverEvent(Component.join(
                            JoinConfiguration.builder().separator(Component.text(", ")).build(),
                            Arrays.stream(EquipmentSlot.values())
                                    .filter(group)
                                    .map(WbsEnums::toPrettyString)
                                    .map(Component::text)
                                    .toList()
                            )
                    );
                })
                .toList();

        Component slotGroups = Component.join(JoinConfiguration.builder().separator(LINE_START).build(), slotComponents);

        return Component.text("Active Slots: ").append(slotGroups);
    }

    private static Component costs(EnchantmentDefinition definition) {
        TextComponent minimumComponent = Component.text("\n  Minimum: ");
        TextComponent maximumComponent = Component.text("\n  Maximum: ");

        if (definition.maxLevel() <= 1) {
            minimumComponent = minimumComponent.append(Component.text(definition.minimumCost().baseCost()).style(HIGHLIGHT));
            maximumComponent = maximumComponent.append(Component.text(definition.maximumCost().baseCost()).style(HIGHLIGHT));
        } else {
            minimumComponent = minimumComponent
                    .append(
                            Component.text("\n    Base: ").append(
                                    Component.text(definition.minimumCost().baseCost()).style(HIGHLIGHT)
                            )
                    ).append(
                    Component.text("\n    Per Level: ").append(
                            Component.text(definition.minimumCost().additionalPerLevelCost()).style(HIGHLIGHT)
                    )
            );

            maximumComponent = maximumComponent
                    .append(
                            Component.text("\n    Base: ").append(
                                    Component.text(definition.maximumCost().baseCost()).style(HIGHLIGHT)
                            )
                    ).append(
                            Component.text("\n    Per Level: ").append(
                                    Component.text(definition.maximumCost().additionalPerLevelCost()).style(HIGHLIGHT)
                    )
            );
        }

        return Component.text("Enchanting Level Ranges:")
                .append(minimumComponent)
                .append(maximumComponent);
    }

    private static Component anvilCost(EnchantmentDefinition definition) {
        return Component.text("Anvil Cost: ").append(Component.text(definition.anvilCost()).style(HIGHLIGHT));
    }


    @Nullable
    public Component describe(EnchantmentDefinition definition) {
        return describeFunction.apply(definition);
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
