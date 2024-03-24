package wbs.enchants.util;

import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import wbs.enchants.WbsEnchantment;
import wbs.enchants.enchantment.*;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class EnchantmentManager {
    private static final Map<NamespacedKey, WbsEnchantment> REGISTERED_ENCHANTMENTS = new HashMap<>();

    public static final EntangledEnchant ENTANGLED = new EntangledEnchant();
    public static final ExcavatorEnchant EXCAVATOR = new ExcavatorEnchant();
    public static final HarvesterEnchant HARVESTER = new HarvesterEnchant();
    public static final VampiricEnchant VAMPIRIC = new VampiricEnchant();
    public static final VeinMinerEnchant VEIN_MINER = new VeinMinerEnchant();
    public static final BlastMinerEnchant BLAST_MINER = new BlastMinerEnchant();
    public static final VoidWalkerEnchant VOID_WALKER = new VoidWalkerEnchant();
    public static final PlanarBindingEnchant PLANAR_BINDING = new PlanarBindingEnchant();
    public static final DisarmingEnchant DISARMING = new DisarmingEnchant();
    public static final FrenziedEnchant FRENZIED = new FrenziedEnchant();
    public static final LightweightEnchant LIGHTWEIGHT = new LightweightEnchant();
    public static final HellborneEnchant HELLBORNE = new HellborneEnchant();
    public static final DecayEnchant DECAY = new DecayEnchant();
    public static final ScorchingEnchant SCORCHING = new ScorchingEnchant();
    public static final FrostburnEnchant FROSTBURN = new FrostburnEnchant();
    public static final EnderShotEnchant ENDER_SHOT = new EnderShotEnchant();
    public static final ImmortalEnchant IMMORTAL = new ImmortalEnchant();
    public static final DualWieldEnchant DUAL_WIELD = new DualWieldEnchant();
    public static final PinpointEnchant PINPOINT = new PinpointEnchant();
    // public static final FrictionlessEnchant FRICTIONLESS = new FrictionlessEnchant();
    public static final UnshakableEnchant UNSHAKABLE = new UnshakableEnchant();
    public static final TransferenceEnchant TRANSFERENCE = new TransferenceEnchant();
    public static final UnsinkableEnchant UNSINKABLE = new UnsinkableEnchant();
    public static final DefusalEnchant DEFUSAL = new DefusalEnchant();
    public static final PilferingEnchant PILFERING = new PilferingEnchant();
    public static final ResilienceEnchant RESILIENCE = new ResilienceEnchant();
    public static final ManathirstEnchant MANATHIRST = new ManathirstEnchant();
    public static final ShulkenforcedEnchant SHULKENFORCED = new ShulkenforcedEnchant();
    public static final DivineResonanceEnchant DIVINE_RESONANCE = new DivineResonanceEnchant();
    public static final AridityEnchant ARIDITY = new AridityEnchant();
    public static final LavaWalkerEnchant LAVA_WALKER = new LavaWalkerEnchant();

    public static void register(WbsEnchantment enchantment) {
        REGISTERED_ENCHANTMENTS.put(enchantment.getKey(), enchantment);
    }

    public static List<WbsEnchantment> getRegistered() {
        return REGISTERED_ENCHANTMENTS.values().stream()
                .sorted(Comparator.comparing(WbsEnchantment::getKey))
                .collect(Collectors.toList());
    }

    public static Set<Enchantment> getAllEnchants() {
        throw new NotImplementedException();
    }

    public static boolean containsEnchantment(WbsEnchantment enchantment, ItemStack stack) {
        return stack.getEnchantments().containsKey(enchantment);
    }

    public static int getLevel(WbsEnchantment enchantment, ItemStack stack) {
        return stack.getEnchantmentLevel(enchantment);
    }
}
