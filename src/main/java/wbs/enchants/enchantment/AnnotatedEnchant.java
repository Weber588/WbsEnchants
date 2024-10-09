package wbs.enchants.enchantment;

import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.MapInitializeEvent;
import org.jetbrains.annotations.NotNull;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

import java.util.List;

public class AnnotatedEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "Maps with this enchantment will show all structures it can, " +
            "even if you haven't explored them yet!";

    public AnnotatedEnchant() {
        super("annotated", DEFAULT_DESCRIPTION);

        supportedItems = WbsEnchantsBootstrap.MAPS;
        exclusiveWith = WbsEnchantsBootstrap.EXCLUSIVE_SET_MAPS;
        weight = 5;

        targetDescription = "Map";
    }

    @Override
    public String getDefaultDisplayName() {
        return "Annotated";
    }

    @EventHandler
    public void onMapRender(MapInitializeEvent event) {
        // TODO: Add all possibly icons based on structure types
    }

    @Override
    public @NotNull List<TagKey<Enchantment>> addToTags() {
        List<TagKey<Enchantment>> addTo = super.addToTags();

        addTo.add(WbsEnchantsBootstrap.EXCLUSIVE_SET_MAPS);

        return addTo;
    }
}
