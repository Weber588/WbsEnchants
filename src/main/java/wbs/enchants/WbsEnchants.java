package wbs.enchants;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import wbs.enchants.command.*;
import wbs.enchants.events.AnvilEvents;
import wbs.enchants.events.LeashEvents;
import wbs.enchants.events.LootGenerationBlocker;
import wbs.enchants.events.MapEvents;
import wbs.enchants.events.enchanting.EnchantingTableEvents;
import wbs.utils.util.WbsFileUtil;
import wbs.utils.util.commands.brigadier.WbsCommand;
import wbs.utils.util.commands.brigadier.WbsErrorsSubcommand;
import wbs.utils.util.commands.brigadier.WbsReloadSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.string.RomanNumerals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public final class WbsEnchants extends WbsPlugin {
    private static WbsEnchants instance;
    public static WbsEnchants getInstance() {
        return instance;
    }

    public EnchantsSettings settings;
    @Override
    public EnchantsSettings getSettings() {
        return settings;
    }

    @Override
    public void onEnable() {
        instance  = this;
        settings = new EnchantsSettings(this);

        WbsCommand.getStatic(this,
                "customenchantments",
                "Commands relating to the WbsEnchantments plugin."
        ).addAliases(
                "cench",
                "customenchants"
        ).addSubcommands(
                new SubcommandInfo(this),
                new SubcommandFullInfo(this),
                new SubcommandList(this),
                new SubcommandHeld(this),
                new SubcommandAdd(this),
                new SubcommandClear(this),
                new SubcommandTagInfo(this),
                new SubcommandGuidebook(this),
                new SubcommandSupported(this),
                new SubcommandBlock(this),
                new SubcommandEnchant(this),
                WbsReloadSubcommand.getStatic(this, settings),
                WbsErrorsSubcommand.getStatic(this, settings)
        ).register();

        registerListener(new LeashEvents());
        registerListener(new MapEvents());
        registerListener(new AnvilEvents());
        registerListener(new LootGenerationBlocker());
        registerListener(new EnchantingTableEvents());

        settings.reload();

        new SharedEventHandler(this).start();

        buildResourcePack();

        EnchantManager.getCustomRegistered().forEach(definition -> {
            definition.registerGenerationContexts();
            definition.registerEvents();
        });
    }

    private void buildResourcePack() {
        Map<String, String> lang = new HashMap<>();

        for (WbsEnchantment enchantment : EnchantManager.getCustomRegistered()) {
            Component description = enchantment.description();
            if (description != null) {
                lang.put("enchantment." + enchantment.key().namespace() + "." + enchantment.key().value() + ".desc",
                        PlainTextComponentSerializer.plainText().serialize(description)
                );
            }
        }

        // Fix roman numerals over level 10, up to 255 (max level for enchants)
        for (int level = 1; level < 256; level++) {
            lang.put("enchantment.level." + level, RomanNumerals.toRoman(level));
        }

        File langFile = getDataPath().resolve("resourcepack/assets/" + getName().toLowerCase() + "/lang/en_us").toFile();
        boolean updatedFile = WbsFileUtil.writeJSONToFile(langFile, lang);
        saveResource("resourcepack/pack.mcmeta", getSettings().isDeveloperMode());

        if (updatedFile) {
            try {
                WbsFileUtil.zipFolder(
                        getDataPath().resolve("resourcepack").toFile(),
                        getDataPath().resolve(getName().toLowerCase() + "_resource_pack.zip").toString()
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            writeToExternalPlugins();
        }
    }

    // TODO: Move this and other resource pack utils into WbsUtils since more of my plugins are using it
    private void writeToExternalPlugins() {
        String name = getName();
        String namespace = name.toLowerCase();

        if (Bukkit.getPluginManager().getPlugin("ResourcePackManager") != null) {
            getComponentLogger().info(Component.text("ResourcePackManager detected! Injecting resource pack.").color(NamedTextColor.GREEN));
            getComponentLogger().info(Component.text("Note: This will load last unless you add \"" + name + "\" to the priority list in ResourcePackManager/config.yml").color(NamedTextColor.GREEN));

            try {
                Files.copy(getDataPath().resolve(
                                namespace + "_resource_pack.zip"),
                        Path.of("plugins/ResourcePackManager/mixer/" + namespace + "_resource_pack.zip"),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                getLogger().severe("Failed to copy resource pack to ResourcePackManager/mixer!");
            }
            /*
            ResourcePackManagerAPI.registerResourcePack(
                    getName(),
                    "WbsWandcraft/wbswandcraft_resource_pack.zip",
                    false,
                    false,
                    true,
                    true,
                    "wbswandcraft:wandcraft reload"
            );
             */
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}
