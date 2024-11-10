package wbs.enchants;

import wbs.enchants.enchantment.*;
import wbs.enchants.enchantment.curse.*;
import wbs.utils.util.WbsFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class EnchantManager {
    public static final double DEFAULT_COST = 1000;
    public static final double DEFAULT_COST_MODIFIER = 0.4;
    public static final double DEFAULT_REMOVAL_COST = 100;
    public static final double DEFAULT_EXTRACT_COST = 2000;
    public static final boolean DEFAULT_USABLE_ANYWHERE = false;

    private static final List<WbsEnchantment> REGISTERED_ENCHANTMENTS = new LinkedList<>();

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
    public static final WickedEnchant WICKED = new WickedEnchant();
    public static final CrushingEnchant CRUSHING = new CrushingEnchant();
    public static final HandheldEnchant HANDHELD = new HandheldEnchant();
    public static final HogsbaneEnchant HOGSBANE = new HogsbaneEnchant();
    public static final ButcheringEnchant BUTCHERING = new ButcheringEnchant();
    public static final SoulTouchEnchant SOUL_TOUCH = new SoulTouchEnchant();
    public static final SoulstealingEnchant SOULSTEALING = new SoulstealingEnchant();

    public static final CurseRebuking CURSE_REBUKING = new CurseRebuking();
    public static final CurseVanilla CURSE_VANILLA = new CurseVanilla();
    public static final CurseExotic CURSE_EXOTIC = new CurseExotic();
    public static final CurseStumbling CURSE_STUMBLING = new CurseStumbling();
    public static final CurseInsomnia CURSE_INSOMNIA = new CurseInsomnia();
    public static final CurseMidas CURSE_MIDAS = new CurseMidas();
    public static final CurseTurbulence CURSE_TURBULENCE = new CurseTurbulence();
    public static final CurseVoiding CURSE_VOIDING = new CurseVoiding();
    public static final CurseErosion CURSE_EROSION = new CurseErosion();
    public static final CurseSplintering CURSE_SPLINTERING = new CurseSplintering();
    public static final CurseTheEnd CURSE_THE_END = new CurseTheEnd();
    public static final CurseMercy CURSE_MERCY = new CurseMercy();

    public static void register(WbsEnchantment enchantment) {
        REGISTERED_ENCHANTMENTS.add(enchantment);
    }

    public static void buildDatapack() {
        final String datapackZipPath = "datapack.zip";
        final String datapackFolderPath = "datapack";

        File datapackZip = new File(WbsEnchants.getInstance().getDataFolder(), datapackZipPath);
        if (!datapackZip.exists() || WbsEnchants.getInstance().settings.isDeveloperMode()) {
            WbsEnchants.getInstance().saveResource(datapackZipPath, true);

            try {
                WbsFileUtil.unzip(datapackZip.getAbsolutePath(), WbsEnchants.getInstance().getDataFolder().getAbsolutePath());
                datapackZip.delete();
            } catch (IOException e) {
                throw new RuntimeException("Failed to unzip datapack archive.", e);
            }
        }

        File datapackFolder = new File(WbsEnchants.getInstance().getDataFolder(), datapackFolderPath);

        try {
            WbsFileUtil.zipFolder(datapackFolder, WbsEnchants.getInstance().getDataFolder().getAbsolutePath()
                    + File.separator + ".."
                    + File.separator + ".."
                    + File.separator + "world"
                    + File.separator + "datapacks"
                    + File.separator + "WbsEnchants.zip");
        } catch (IOException e) {
            throw new RuntimeException("Failed to re-archive datapack to datapacks folder", e);
        }
    }

    public static List<WbsEnchantment> getRegistered() {
        return Collections.unmodifiableList(REGISTERED_ENCHANTMENTS);
    }
}
