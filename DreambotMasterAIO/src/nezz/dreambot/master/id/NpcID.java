package nezz.dreambot.master.id;

/**
 * Curated subset of {@code net.runelite.api.NpcID}.
 *
 * <p>F2P questgivers, common training NPCs and slayer task targets the
 * MasterAIO references.</p>
 *
 * <p>Full canonical list:
 * <a href="https://github.com/open-osrs/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/NpcID.java">
 *   open-osrs runelite-api/NpcID.java</a></p>
 */
public final class NpcID {

    private NpcID() { }

    // ── Tutorial Island ──────────────────────────────────────────────────────
    public static final int RUNESCAPE_GUIDE       = 945;
    public static final int SURVIVAL_EXPERT       = 943;
    public static final int MASTER_CHEF           = 906;
    public static final int QUEST_GUIDE           = 944;
    public static final int MINING_INSTRUCTOR     = 941;
    public static final int COMBAT_INSTRUCTOR     = 4287;
    public static final int FINANCIAL_ADVISOR     = 947;
    public static final int BROTHER_BRACE         = 942;
    public static final int MAGIC_INSTRUCTOR      = 946;
    public static final int GIANT_RAT_TUT         = 2854;

    // ── F2P quest NPCs ───────────────────────────────────────────────────────
    public static final int COOK                  = 278;     // Lumbridge cook
    public static final int FARMER_FRED           = 757;     // Sheep Shearer
    public static final int SHEEP_SS              = 759;     // black-fleeced sheep
    public static final int FATHER_LAWRENCE       = 4905;    // Restless Ghost
    public static final int FATHER_URHNEY         = 5439;    // Restless Ghost
    public static final int RESTLESS_GHOST_NPC    = 5446;    // Restless Ghost
    public static final int VERONICA              = 5440;    // Ernest the Chicken
    public static final int LEELA                 = 5441;    // Ernest the Chicken
    public static final int OZIACH                = 740;     // Dragon Slayer
    public static final int ROMEO                 = 754;     // Romeo & Juliet
    public static final int JULIET                = 755;
    public static final int FATHER_LAWRENCE_RJ    = 756;
    public static final int APOTHECARY            = 750;
    public static final int IMP_CATCHER_WIZARD    = 736;
    public static final int HETTY                 = 720;     // Witch's Potion
    public static final int GENERAL_BENTNOZE      = 4494;    // Goblin Diplomacy
    public static final int GENERAL_WARTFACE      = 4495;    // Goblin Diplomacy
    public static final int GOBLIN_DIPLOMACY_GUY  = 4493;    // Wartface's lieutenant
    public static final int COUNT_DRAYNOR         = 758;     // Vampyre Slayer
    public static final int MORGAN                = 759;     // overlap intentional in source list
    public static final int DR_HARLOW             = 760;     // Vampyre Slayer
    public static final int MANDRITH              = 7531;    // Misthalin Mystery

    // ── F2P combat training NPCs (common) ────────────────────────────────────
    public static final int CHICKEN               = 3316;
    public static final int COW                   = 81;
    public static final int COW_CALF              = 397;
    public static final int GIANT_RAT             = 86;
    public static final int GOBLIN                = 3029;
    public static final int MAN                   = 3014;
    public static final int WOMAN                 = 3018;
    public static final int GUARD                 = 3010;
    public static final int AL_KHARID_WARRIOR     = 3103;
    public static final int MONK                  = 3074;
    public static final int MONK_OF_ZAMORAK       = 8717;
    public static final int HILL_GIANT            = 2098;
    public static final int MOSS_GIANT            = 121;
    public static final int LESSER_DEMON          = 2005;
    public static final int GREATER_DEMON         = 84;
    public static final int FLESH_CRAWLER         = 132;
    public static final int OGRESS_WARRIOR        = 7995;
    public static final int OGRESS_SHAMAN         = 7996;
    public static final int DARK_WARRIOR          = 7867;
    public static final int ROCK_CRAB             = 1265;
    public static final int SAND_CRAB             = 5810;
    public static final int AMMONITE_CRAB         = 7757;
    public static final int SWAMP_CRAB            = 7800;

    // ── Banking NPCs / GE ────────────────────────────────────────────────────
    public static final int BANKER                = 487;
    public static final int GRAND_EXCHANGE_CLERK  = 2244;

    // ── Range / Magic training ───────────────────────────────────────────────
    public static final int SAFE_SPOT_OGRE        = 116;
    public static final int CRABS_GENERIC         = 1265;
}
