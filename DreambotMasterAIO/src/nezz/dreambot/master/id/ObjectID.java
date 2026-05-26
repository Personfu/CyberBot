package nezz.dreambot.master.id;

/**
 * Curated subset of {@code net.runelite.api.ObjectID}. Holds the most-used
 * GameObject IDs for tutorial, F2P quests, common skill training spots, and
 * banking. Full canonical list:
 * <a href="https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/ObjectID.java">
 *   runelite-api/ObjectID.java</a>.
 */
public final class ObjectID {

    private ObjectID() { }

    // ── Tutorial ─────────────────────────────────────────────────────────────
    public static final int TUTORIAL_DOOR_GIELINOR  = 9716;
    public static final int TUTORIAL_DOOR_KITCHEN   = 9717;
    public static final int TUTORIAL_GATE           = 9718;
    public static final int TUTORIAL_LADDER_DOWN    = 9719;
    public static final int TUTORIAL_LADDER_UP      = 9720;
    public static final int TUTORIAL_RANGE          = 114;
    public static final int TUTORIAL_TREE           = 9730;
    public static final int TUTORIAL_FIRE           = 26185;
    public static final int TUTORIAL_FISHING_SPOT   = 3317;
    public static final int TUTORIAL_TIN_ROCKS      = 10080;
    public static final int TUTORIAL_COPPER_ROCKS   = 10081;
    public static final int TUTORIAL_FURNACE        = 10082;
    public static final int TUTORIAL_ANVIL          = 2097;
    public static final int TUTORIAL_BANK_BOOTH     = 10083;
    public static final int TUTORIAL_POLL_BOOTH     = 26815;

    // ── Common doors / gates ─────────────────────────────────────────────────
    public static final int DOOR_GENERIC            = 1530;
    public static final int GATE_GENERIC            = 1551;
    public static final int LADDER_UP               = 17385;
    public static final int LADDER_DOWN             = 17384;

    // ── Banks ────────────────────────────────────────────────────────────────
    public static final int BANK_BOOTH_VARROCK_WEST = 10355;
    public static final int BANK_BOOTH_VARROCK_EAST = 10356;
    public static final int BANK_BOOTH_GE           = 10061;
    public static final int BANK_BOOTH_LUMBRIDGE_TOP = 10583;
    public static final int BANK_BOOTH_DRAYNOR      = 6943;
    public static final int BANK_BOOTH_FALADOR_WEST = 11758;
    public static final int BANK_BOOTH_FALADOR_EAST = 24101;

    // ── Trees ────────────────────────────────────────────────────────────────
    public static final int TREE_NORMAL             = 1276;
    public static final int TREE_DEAD               = 1278;
    public static final int TREE_OAK                = 10820;
    public static final int TREE_WILLOW             = 10819;
    public static final int TREE_MAPLE              = 10832;
    public static final int TREE_YEW                = 10821;
    public static final int TREE_MAGIC              = 10834;
    public static final int TREE_REDWOOD            = 29670;
    public static final int TREE_TEAK               = 9036;
    public static final int TREE_MAHOGANY           = 9034;
    public static final int TREE_ACHEY              = 2023;
    public static final int TREE_DRAMEN             = 1292;

    // ── Mining rocks ─────────────────────────────────────────────────────────
    public static final int ROCKS_TIN               = 11360;
    public static final int ROCKS_COPPER            = 10943;
    public static final int ROCKS_IRON              = 11365;
    public static final int ROCKS_SILVER            = 11368;
    public static final int ROCKS_COAL              = 11366;
    public static final int ROCKS_GOLD              = 11370;
    public static final int ROCKS_MITHRIL           = 11372;
    public static final int ROCKS_ADAMANTITE        = 11374;
    public static final int ROCKS_RUNITE            = 11376;
    public static final int ROCKS_GEM               = 11381;
    public static final int ROCKS_BLURITE           = 11378;

    // ── Fishing spots (some are NPCs, see NpcID.FISHING_SPOT_*) ──────────────
    public static final int FISHING_SPOT_SHRIMP     = 1530;     // varies; uses NPC normally
    public static final int FISHING_SPOT_TROUT      = 1526;     // ditto
    public static final int FISHING_SPOT_LOBSTER    = 1522;
    public static final int FISHING_SPOT_SHARK      = 1525;
    public static final int FISHING_SPOT_KARAMBWAN  = 4712;

    // ── Furnace / smelting ───────────────────────────────────────────────────
    public static final int FURNACE_EDGEVILLE       = 16469;
    public static final int FURNACE_FALADOR         = 24009;
    public static final int FURNACE_AL_KHARID       = 24010;
    public static final int ANVIL                   = 2097;

    // ── Altars (Prayer / RC) ─────────────────────────────────────────────────
    public static final int ALTAR_GENERIC           = 409;
    public static final int CHAOS_ALTAR_WILDY       = 411;
    public static final int CHAOS_ALTAR_VARROCK_SEWER = 411;
    public static final int RC_AIR_ALTAR            = 14897;
    public static final int RC_MIND_ALTAR           = 14899;
    public static final int RC_WATER_ALTAR          = 14901;
    public static final int RC_EARTH_ALTAR          = 14903;
    public static final int RC_FIRE_ALTAR           = 14905;
    public static final int RC_BODY_ALTAR           = 14907;

    // ── Varrock Sewers ─────────────────────────────────────────────────────────
    /** Manhole cover on the surface — interact "Open" then "Climb-down". */
    public static final int MANHOLE_VARROCK_SEWER_LID = 886;
    /** Opened manhole — interact "Climb-down". */
    public static final int MANHOLE_VARROCK_SEWER_OPEN = 2574;
    /** Cobweb blocking the path to the Moss Giant back room — slash with knife. */
    public static final int COBWEB_VARROCK_SEWER      = 733;

    // ── F2P quest objects ────────────────────────────────────────────────────
    public static final int LUMBRIDGE_KITCHEN_LADDER = 23823;
    public static final int VARROCK_CHURCH_DOOR     = 11797;
    public static final int DRAYNOR_MANOR_FRONT_DOOR = 134;
    public static final int DRAYNOR_MANOR_BACK_GATE = 4252;
}
