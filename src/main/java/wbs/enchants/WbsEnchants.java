package wbs.enchants;

import com.google.gson.Gson;
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
import wbs.utils.util.WbsFileUtil;
import wbs.utils.util.commands.brigadier.WbsCommand;
import wbs.utils.util.commands.brigadier.WbsErrorsSubcommand;
import wbs.utils.util.commands.brigadier.WbsReloadSubcommand;
import wbs.utils.util.plugin.WbsPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class WbsEnchants extends WbsPlugin {
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
                new SubcommandList(this),
                new SubcommandHeld(this),
                new SubcommandAdd(this),
                new SubcommandClear(this),
                new SubcommandTagInfo(this),
                new SubcommandGuidebook(this),
                new SubcommandSupported(this),
                new SubcommandBlock(this),
                WbsReloadSubcommand.getStatic(this, settings),
                WbsErrorsSubcommand.getStatic(this, settings)
        ).register();

        registerListener(new LeashEvents());
        registerListener(new MapEvents());
        registerListener(new AnvilEvents());
        registerListener(new LootGenerationBlocker());

        settings.reload();

        new SharedEventHandler(this).start();

        buildResourcePack();
    }

    private void buildResourcePack() {
        Gson gson = new Gson();

        Map<String, String> lang = new HashMap<>();

        for (WbsEnchantment enchantment : EnchantManager.getCustomRegistered()) {
            Component description = enchantment.description();
            if (description != null) {
                lang.put("enchantment." + enchantment.key().namespace() + "." + enchantment.key().value() + ".desc",
                        PlainTextComponentSerializer.plainText().serialize(description)
                );
            }
        }

        writeToJSONFile(gson, lang, "resourcepack/assets/" + getName().toLowerCase() + "/lang/en_us");
        saveResource("resourcepack/pack.mcmeta", false);

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

    private void writeToJSONFile(Gson gson, Object object, String path) {
        try {
            // TODO: Make the written language file configurable
            File file = getDataPath().resolve(path + ".json").toFile();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(object, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
