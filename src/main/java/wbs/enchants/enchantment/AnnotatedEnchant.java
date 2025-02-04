package wbs.enchants.enchantment;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.MapInitializeEvent;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.WbsEnchantsBootstrap;

public class AnnotatedEnchant extends WbsEnchantment {
    private static final String DEFAULT_DESCRIPTION = "Maps with this enchantment will show all structures it can, " +
            "even if you haven't explored them yet!";

    public AnnotatedEnchant() {
        super("annotated", DEFAULT_DESCRIPTION);

        getDefinition()
                .supportedItems(WbsEnchantsBootstrap.MAPS)
                .exclusiveWith(WbsEnchantsBootstrap.EXCLUSIVE_SET_MAPS)
                .addInjectInto(WbsEnchantsBootstrap.EXCLUSIVE_SET_MAPS)
                .weight(5)
                .targetDescription("Map");
    }

    @EventHandler
    public void onMapRender(MapInitializeEvent event) {
        // TODO: Add all possibly icons based on structure types
    }
}
