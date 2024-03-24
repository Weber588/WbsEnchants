package wbs.enchants;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AnvilSettings {
    private AnvilSettings() {}

    private static final Multimap<Material, Material> REPAIR_MATERIALS = HashMultimap.create();

    public static boolean handleAnvilMechanics = false;

    @NotNull
    public static Collection<Material> getRepairMaterialsFor(Material material) {
        return REPAIR_MATERIALS.get(material);
    }

    public static boolean isRepairMaterialFor(Material base, Material repairCheck) {
        return getRepairMaterialsFor(base).contains(repairCheck);
    }

    public static boolean isRepairItemFor(ItemStack base, ItemStack repairCheck) {
        return getRepairMaterialsFor(base.getType()).contains(repairCheck.getType());
    }

    public static void configure(YamlConfiguration configuration) {
        handleAnvilMechanics =  configuration.getBoolean()
    }
}
