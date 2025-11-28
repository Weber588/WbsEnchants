package wbs.enchants;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.enchants.definition.EnchantmentDefinition;
import wbs.enchants.definition.EnchantmentExtension;
import wbs.enchants.enchantment.*;
import wbs.enchants.enchantment.curse.*;
import wbs.enchants.enchantment.shulkerbox.*;
import wbs.utils.util.WbsFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class EnchantManager {
    private static final List<WbsEnchantment> REGISTERED_ENCHANTMENTS = new LinkedList<>();
    private static final List<EnchantmentDefinition> EXTERNAL_DEFINITIONS = new LinkedList<>();

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
    public static final StuttershotEnchant STUTTERSHOT = new StuttershotEnchant();
    public static final EchoingEnchant ECHOING = new EchoingEnchant();
    public static final DimensionalStabilityEnchant DIMENSIONAL_STABILITY = new DimensionalStabilityEnchant();
    public static final AnimaConduitEnchant ANIMA_CONDUIT = new AnimaConduitEnchant();
    public static final TreeFellerEnchant TREE_FELLER = new TreeFellerEnchant();
    public static final SpiritheartEnchant SPIRITHEART = new SpiritheartEnchant();
    public static final AfterlifeEnchant AFTERLIFE = new AfterlifeEnchant();
    public static final CavingEnchant CAVING = new CavingEnchant();
    public static final EarthboundEnchant EARTHBOUND = new EarthboundEnchant();
    public static final DisengageEnchant DISENGAGE = new DisengageEnchant();
    public static final EnderspiteEnchant ENDERSPITE = new EnderspiteEnchant();
    public static final FrostcreepEnchant FROSTCREEP = new FrostcreepEnchant();
    // public static final HellfireEnchant HELLFIRE = new HellfireEnchant();
    public static final OutreachEnchant OUTREACH = new OutreachEnchant();
    public static final HydrophobicEnchant HYDROPHOBIC = new HydrophobicEnchant();
    public static final PyroclasticEnchant PYROCLASTIC = new PyroclasticEnchant();
    public static final SurfaceMinerEnchant SURFACE_MINER = new SurfaceMinerEnchant();
    public static final CarryingEnchant CARRYING = new CarryingEnchant();
    // public static final SnackingEnchant SNACKING = new SnackingEnchant();
    public static final RefillEnchant REFILL = new RefillEnchant();
    public static final SiphoningEnchant SIPHONING = new SiphoningEnchant();
    public static final PlacingEnchant PLACING = new PlacingEnchant();
    public static final DiscardingEnchant DISCARDING = new DiscardingEnchant();
    public static final BloomingEnchant BLOOMING = new BloomingEnchant();
    public static final GhostPactEnchant GHOST_PACT = new GhostPactEnchant();
    public static final FangedEnchant FANGED = new FangedEnchant();
    public static final MagneticEnchant MAGNETIC = new MagneticEnchant();
    public static final CastingEnchant CASTING = new CastingEnchant();
    public static final MulticastEnchant MULTICAST = new MulticastEnchant();
    public static final AutoReelEnchant AUTO_REEL = new AutoReelEnchant();
    public static final HellHookEnchant HELL_HOOK = new HellHookEnchant();
    public static final ArcticStrikeEnchant ARCTIC_STRIKE = new ArcticStrikeEnchant();
    public static final TemperedEnchant TEMPERED = new TemperedEnchant();
    public static final NihilEnchant NIHIL = new NihilEnchant();
    public static final QuickRideEnchant QUICK_RIDE = new QuickRideEnchant();
    public static final FleecingEnchant FLEECING = new FleecingEnchant();
    public static final CautiousnessEnchant CAUTIOUSNESS = new CautiousnessEnchant();
    public static final ReinforcedEnchant REINFORCED = new ReinforcedEnchant();
    public static final ReflexiveEnchant REFLEXIVE = new ReflexiveEnchant();
    public static final DemetrienEnchant DEMETRIEN = new DemetrienEnchant();
    public static final ThrowingEnchant THROWING = new ThrowingEnchant();
    public static final GallopingEnchant GALLOPING = new GallopingEnchant();
    public static final LeapingEnchant LEAPING = new LeapingEnchant();
    public static final CavalrousEnchant CAVALROUS = new CavalrousEnchant();
    public static final FungalHeartEnchant FUNGAL_HEART = new FungalHeartEnchant();
    public static final AbyssMinerEnchant ABYSS_MINER = new AbyssMinerEnchant();
    public static final AethereanEnchant AETHEREAN = new AethereanEnchant();
    public static final BuoyancyEnchant BUOYANCY = new BuoyancyEnchant();
    public static final AnnotatedEnchant ANNOTATED = new AnnotatedEnchant();
    public static final HavenEnchant HAVEN = new HavenEnchant();
    public static final SnaringEnchant SNARING = new SnaringEnchant();
    public static final HeavingEnchant HEAVING = new HeavingEnchant();
    public static final WhirlingEnchant WHIRLING = new WhirlingEnchant();
    public static final HoveringEnchant HOVERING = new HoveringEnchant();
    public static final RerollingEnchant REROLLING = new RerollingEnchant();
    public static final AmbitiousnessEnchant AMBITIOUSNESS = new AmbitiousnessEnchant();
    public static final RecallEnchant RECALL = new RecallEnchant();
    public static final AncientCryEnchant ANCIENT_CRY = new AncientCryEnchant();
    public static final BreathtakingEnchant BREATHTAKING = new BreathtakingEnchant();
    public static final PlatformerEnchant PLATFORMER = new PlatformerEnchant();
    public static final VenturingEnchant VENTURING = new VenturingEnchant();
    public static final HaulingEnchant HAULING = new HaulingEnchant();
    public static final TrackingEnchant TRACKING = new TrackingEnchant();
    public static final PowerlustEnchant POWERLUST = new PowerlustEnchant();
    public static final IndustriousEnchant INDUSTRIOUS = new IndustriousEnchant();
    public static final StridingEnchant STRIDING = new StridingEnchant();
    public static final FlyFishingEnchant FLY_FISHING = new FlyFishingEnchant();
    public static final LaunchingEnchant LAUNCHING = new LaunchingEnchant();
    public static final SupersonicEnchant SUPERSONIC = new SupersonicEnchant();
    public static final SalvageEnchant SALVAGE = new SalvageEnchant();
    public static final WisdomEnchant WISDOM = new WisdomEnchant();
    public static final PortalWalkerEnchant PORTAL_WALKER = new PortalWalkerEnchant();
    public static final InvocationEnchant INVOCATION = new InvocationEnchant();
    public static final AbjurationEnchant ABJURATION = new AbjurationEnchant();
    public static final InsightfulEnchant INSIGHTFUL = new InsightfulEnchant();

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
    public static final CurseRust CURSE_RUST = new CurseRust();
    public static final CurseRot CURSE_ROT = new CurseRot();
    public static final CurseHypoxia CURSE_HYPOXIA = new CurseHypoxia();
    public static final CurseLimping CURSE_LIMPING = new CurseLimping();

    public static void register(WbsEnchantment enchantment) {
        REGISTERED_ENCHANTMENTS.add(enchantment);
    }
    public static void registerExternal(EnchantmentDefinition definition) {
        EXTERNAL_DEFINITIONS.add(definition);
    }

    public static void buildDatapack() {
        final String datapackZipPath = "datapack.zip";
        final String datapackFolderPath = "datapack";

        File datapackZip = new File(WbsEnchants.getInstance().getDataFolder(), datapackZipPath);
        if (!datapackZip.exists() || WbsEnchants.getInstance().settings.isDeveloperMode()) {
            WbsEnchants.getInstance().saveResource(datapackZipPath, true);

            try {
                WbsFileUtil.unzip(datapackZip.getAbsolutePath(), WbsEnchants.getInstance().getDataFolder().getAbsolutePath());
                if (!datapackZip.delete()) {
                    throw new IOException("Failed to delete datapack zip!");
                }
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

    public static List<WbsEnchantment> getCustomRegistered() {
        return Collections.unmodifiableList(REGISTERED_ENCHANTMENTS);
    }

    public static List<EnchantmentDefinition> getAllKnownDefinitions() {
        List<EnchantmentDefinition> definitions = new LinkedList<>(EXTERNAL_DEFINITIONS);
        REGISTERED_ENCHANTMENTS.stream().map(EnchantmentExtension::getDefinition).forEach(definitions::add);

        return definitions;

    }

    public static @Nullable EnchantmentDefinition getFromKey(Key enchantKey) {
        return getAllKnownDefinitions().stream()
                .filter(enchant -> enchant.key().equals(enchantKey))
                .findFirst()
                .orElse(null);
    }

    public static @Nullable WbsEnchantment getCustomFromKey(Key enchantKey) {
        return REGISTERED_ENCHANTMENTS.stream()
                .filter(enchant -> enchant.key().equals(enchantKey))
                .findFirst()
                .orElse(null);

    }

    public static EnchantmentDefinition getExternalDefinition(Key enchantKey) {
        return EXTERNAL_DEFINITIONS.stream()
                .filter(def -> def.key().equals(enchantKey))
                .findFirst()
                .orElse(null);
    }

    public static void unregister(WbsEnchantment enchant) {
        try {
            enchant.getDefinition().setEnabled(false);
            REGISTERED_ENCHANTMENTS.remove(enchant);
        } catch (Exception ex) {
            System.out.println("Failed to unregister custom enchantment " + enchant.key().asString() + ".");
            ex.printStackTrace(System.out);
        }
    }

    public static Multimap<String, EnchantmentDefinition> getDefinitionsByNamespace() {
        Multimap<String, EnchantmentDefinition> byNamespace = HashMultimap.create();

        getAllKnownDefinitions().forEach(def -> byNamespace.put(def.key().namespace(), def));

        return byNamespace;
    }

    public static List<String> getNamespaces() {
        LinkedList<String> namespaces = new LinkedList<>(getDefinitionsByNamespace().keySet());

        namespaces.sort((a, b) -> {
            if (a.equalsIgnoreCase("minecraft")) {
                return 1;
            } else if (a.equalsIgnoreCase(WbsEnchantsBootstrap.NAMESPACE)) {
                return 1;
            } else {
                return a.compareTo(b);
            }
        });

        return namespaces;
    }

    @Nullable
    public static EnchantmentDefinition getFrom(@NotNull Enchantment from) {
        return getFromKey(from.key());
    }

    public static boolean isManaged(EnchantmentDefinition definition) {
        return getCustomFromKey(definition.key()) != null;
    }

    public static boolean isManaged(Enchantment enchantment) {
        return getCustomFromKey(enchantment.key()) != null;
    }
}
