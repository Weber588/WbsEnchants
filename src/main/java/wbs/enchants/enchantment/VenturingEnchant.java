package wbs.enchants.enchantment;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.helper.MovementEnchant;
import wbs.enchants.util.CooldownManager;
import wbs.utils.util.string.WbsStrings;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class VenturingEnchant extends WbsEnchantment implements MovementEnchant {
    private static final @NotNull String DEFAULT_DESCRIPTION = "Shows the name of structures when you enter them.";

    private static final List<KeyMapper> REGEX_KEY_MAP = new LinkedList<>();
    private static final List<Key> IGNORED_KEYS = new LinkedList<>();
    private static final int MESSAGE_COOLDOWN = Ticks.TICKS_PER_SECOND * 30;

    // TODO: Make these configurable
    static {
        IGNORED_KEYS.add(Key.key("minecraft:buried_treasure"));

        addKeyMapper("minecraft:fortress", "Nether Fortress");
        addKeyMapper("minecraft:monument", "Ocean Monument");
        addKeyMapper("minecraft:ruined_portal.*", "Ruined Portal");

        // Vanilla endings
        addKeyMapper("[_/]mesa$", " (Mesa)");
        addKeyMapper("[_/]cold$", " (Cold)");
        addKeyMapper("[_/]warm$", " (Warm)");
        addKeyMapper("[_/]desert$", " (Desert)");
        addKeyMapper("[_/]jungle$", " (Jungle)");
        addKeyMapper("[_/]swamp$", " (Swamp)");
        addKeyMapper("[_/]plains$", " (Plains)");
        addKeyMapper("[_/]savanna$", " (Savanna)");
        addKeyMapper("[_/]snowy$", " (Snowy)");
        addKeyMapper("[_/]taiga$", " (Taiga)");
        addKeyMapper("[_/]beached$", " (Beached)");

        // Non-vanilla endings, mostly for datapacks I've used
        addKeyMapper("[_/]surface$", "");
        addKeyMapper("[_/]underground$", "");
        addKeyMapper("(.*)([_/]grassy)$", "Grassy $1");
        addKeyMapper("[_/]dark_oak$", "");
        addKeyMapper("[_/]oak$", "");
        addKeyMapper("[_/]birch$", "");
        addKeyMapper("[_/]spruce$", "");
        //noinspection SpellCheckingInspection
        addKeyMapper("savana$", "");

        addKeyMapper("[_/]", " ");

        // Remove namespaces after, so they can be used above
        addKeyMapper(".*:", "");

    }

    private static void addKeyMapper(@RegExp String regex, String replace) {
        REGEX_KEY_MAP.add(new KeyMapper(regex, replace));
    }

    public VenturingEnchant() {
        super("venturing", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR);
    }

    @Override
    public void onChangeBlock(Player player, Block oldBlock, Block newBlock) {
        ItemStack item = getHighestEnchanted(player);

        if (item != null) {
            List<GeneratedStructure> oldStructures = getStructuresAt(oldBlock.getLocation(), true);
            List<GeneratedStructure> newStructures = getStructuresAt(newBlock.getLocation(), true);

            newStructures.removeIf(check ->
                    oldStructures.stream().anyMatch(check2 ->
                            check2.getStructure().getStructureType().key().equals(check.getStructure().getStructureType().key())
                    )
            );

            if (!newStructures.isEmpty()) {
                Structure structure = newStructures.getFirst().getStructure();
                Key structureKey = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE).getKey(structure);

                if (structureKey != null) {
                    if (!CooldownManager.newCooldown(player, MESSAGE_COOLDOWN, new NamespacedKey(structureKey.namespace(), structureKey.value()))) {
                        return;
                    }
                    sendActionBar("Entering &h" + getStructureKeyString(structureKey), player);
                } else {
                    NamespacedKey key = structure.getStructureType().getKey();
                    if (!CooldownManager.newCooldown(player, MESSAGE_COOLDOWN, key)) {
                        return;
                    }
                    sendActionBar("Entering &Unknown Structure (" + getStructureKeyString(key) + ")", player);
                }
            }
        }
    }

    private static @NotNull String getStructureKeyString(Key structureKey) {
        String asString = structureKey.asString();
        for (KeyMapper mapper : REGEX_KEY_MAP) {
            asString = mapper.apply(asString);
        }

        return WbsStrings.capitalizeAll(asString);
    }

    public List<GeneratedStructure> getStructuresAt(Location location, boolean piecesOnly) {
        return location.getChunk().getStructures().stream()
                .filter(structure -> {
                    if (piecesOnly) {
                        return structure.getPieces().stream()
                                .anyMatch(piece ->
                                        piece.getBoundingBox().contains(location.toVector())
                                );
                    } else {
                        return structure.getBoundingBox().contains(location.toVector());
                    }
                }).collect(Collectors.toList());
    }

    private record KeyMapper(@RegExp String regex, String replace) {
        public String apply(String applyTo) {
            return applyTo.replaceAll(regex, replace);
        }
    }
}
