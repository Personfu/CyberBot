package nezz.dreambot.master.id;

/**
 * Curated subset of {@code net.runelite.api.ItemID} — items the F2P account
 * builder will reference: tutorial drops, F2P quest items, common stackables,
 * smelting bars, fishing fish, runes, food.
 *
 * <p>Full canonical list:
 * <a href="https://github.com/open-osrs/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/ItemID.java">
 *   open-osrs runelite-api/ItemID.java</a>. Keeping names identical so a
 * porting tool can extend this without rename churn.</p>
 */
public final class ItemID {

    private ItemID() { }

    // ── Tutorial / starter ───────────────────────────────────────────────────
    public static final int LOGS                  = 1511;
    public static final int TINDERBOX             = 590;
    public static final int RAW_SHRIMPS           = 317;
    public static final int SHRIMPS               = 315;
    public static final int RAW_ANCHOVIES         = 321;
    public static final int ANCHOVIES             = 319;
    public static final int BREAD_DOUGH           = 2307;
    public static final int BREAD                 = 2309;
    public static final int POT_OF_FLOUR          = 1933;
    public static final int BUCKET_OF_WATER       = 1929;
    public static final int TIN_ORE               = 438;
    public static final int COPPER_ORE            = 436;
    public static final int BRONZE_BAR            = 2349;
    public static final int BRONZE_DAGGER         = 1205;
    public static final int BRONZE_SWORD          = 1277;
    public static final int WOODEN_SHIELD         = 1171;
    public static final int SHORTBOW              = 841;
    public static final int BRONZE_ARROW          = 882;

    // ── Cooking / food ───────────────────────────────────────────────────────
    public static final int RAW_TROUT             = 335;
    public static final int TROUT                 = 333;
    public static final int RAW_SALMON            = 331;
    public static final int SALMON                = 329;
    public static final int RAW_LOBSTER           = 377;
    public static final int LOBSTER               = 379;
    public static final int RAW_SHARK             = 383;
    public static final int SHARK                 = 385;
    public static final int RAW_KARAMBWAN         = 3142;
    public static final int COOKED_KARAMBWAN      = 3144;
    public static final int COOKED_CHICKEN        = 2140;
    public static final int RAW_CHICKEN           = 2138;
    public static final int RAW_BEEF              = 2132;
    public static final int COOKED_MEAT           = 2142;
    public static final int RAW_TUNA              = 359;
    public static final int TUNA                  = 361;
    public static final int RAW_SWORDFISH         = 371;
    public static final int SWORDFISH             = 373;

    // ── Fish bait / tools ────────────────────────────────────────────────────
    public static final int FEATHER               = 314;
    public static final int FISHING_BAIT          = 313;
    public static final int FISHING_ROD           = 307;
    public static final int FLY_FISHING_ROD       = 309;
    public static final int LOBSTER_POT           = 301;
    public static final int HARPOON               = 311;
    public static final int SMALL_FISHING_NET     = 303;
    public static final int OILY_FISHING_ROD      = 1585;
    public static final int BIG_FISHING_NET       = 305;

    // ── Mining picks ─────────────────────────────────────────────────────────
    public static final int BRONZE_PICKAXE        = 1265;
    public static final int IRON_PICKAXE          = 1267;
    public static final int STEEL_PICKAXE         = 1269;
    public static final int BLACK_PICKAXE         = 12297;
    public static final int MITHRIL_PICKAXE       = 1273;
    public static final int ADAMANT_PICKAXE       = 1271;
    public static final int RUNE_PICKAXE          = 1275;
    public static final int DRAGON_PICKAXE        = 11920;
    public static final int CRYSTAL_PICKAXE       = 23680;

    // ── Hatchets ─────────────────────────────────────────────────────────────
    public static final int BRONZE_AXE            = 1351;
    public static final int IRON_AXE              = 1349;
    public static final int STEEL_AXE             = 1353;
    public static final int BLACK_AXE             = 1361;
    public static final int MITHRIL_AXE           = 1355;
    public static final int ADAMANT_AXE           = 1357;
    public static final int RUNE_AXE              = 1359;
    public static final int DRAGON_AXE            = 6739;
    public static final int CRYSTAL_AXE           = 23673;

    // ── Logs ─────────────────────────────────────────────────────────────────
    public static final int OAK_LOGS              = 1521;
    public static final int WILLOW_LOGS           = 1519;
    public static final int MAPLE_LOGS            = 1517;
    public static final int YEW_LOGS              = 1515;
    public static final int MAGIC_LOGS            = 1513;
    public static final int REDWOOD_LOGS          = 19669;
    public static final int TEAK_LOGS             = 6333;
    public static final int MAHOGANY_LOGS         = 6332;
    public static final int ARCTIC_PINE_LOGS      = 10810;

    // ── Ores ─────────────────────────────────────────────────────────────────
    public static final int IRON_ORE              = 440;
    public static final int SILVER_ORE            = 442;
    public static final int COAL                  = 453;
    public static final int GOLD_ORE              = 444;
    public static final int MITHRIL_ORE           = 447;
    public static final int ADAMANTITE_ORE        = 449;
    public static final int RUNITE_ORE            = 451;

    // ── Bars ─────────────────────────────────────────────────────────────────
    public static final int IRON_BAR              = 2351;
    public static final int STEEL_BAR             = 2353;
    public static final int GOLD_BAR              = 2357;
    public static final int MITHRIL_BAR           = 2359;
    public static final int ADAMANTITE_BAR        = 2361;
    public static final int RUNITE_BAR            = 2363;

    // ── F2P runes ────────────────────────────────────────────────────────────
    public static final int AIR_RUNE              = 556;
    public static final int WATER_RUNE            = 555;
    public static final int EARTH_RUNE            = 557;
    public static final int FIRE_RUNE             = 554;
    public static final int MIND_RUNE             = 558;
    public static final int BODY_RUNE             = 559;
    public static final int CHAOS_RUNE            = 562;
    public static final int DEATH_RUNE            = 560;
    public static final int LAW_RUNE              = 563;
    public static final int NATURE_RUNE           = 561;
    public static final int COSMIC_RUNE           = 564;
    public static final int BLOOD_RUNE            = 565;
    public static final int SOUL_RUNE             = 566;
    public static final int ASTRAL_RUNE           = 9075;
    public static final int WRATH_RUNE            = 21880;

    // ── Tiaras / talismans (RC) ──────────────────────────────────────────────
    public static final int AIR_TALISMAN          = 1438;
    public static final int MIND_TALISMAN         = 1448;
    public static final int WATER_TALISMAN        = 1444;
    public static final int EARTH_TALISMAN        = 1440;
    public static final int FIRE_TALISMAN         = 1442;
    public static final int BODY_TALISMAN         = 1446;
    public static final int LAW_TALISMAN          = 1458;
    public static final int NATURE_TALISMAN       = 1462;

    // ── Coins / currency / bones ─────────────────────────────────────────────
    public static final int COINS_995             = 995;
    public static final int BLOOD_MONEY           = 13307;
    public static final int PLATINUM_TOKEN        = 13204;
    public static final int BONES                 = 526;
    public static final int BIG_BONES             = 532;
    public static final int BABY_DRAGON_BONES     = 534;
    public static final int DRAGON_BONES          = 536;
    public static final int SUPERIOR_DRAGON_BONES = 22124;
    public static final int DAGANNOTH_BONES       = 6729;
    public static final int WYVERN_BONES          = 6812;

    // ── F2P quest specific items ─────────────────────────────────────────────
    public static final int POT                   = 1931;
    public static final int FLOUR                 = 1933;
    public static final int EGG                   = 1944;
    public static final int BUCKET                = 1925;
    public static final int JUG                   = 1935;
    public static final int RAW_RAT_MEAT          = 2134;
    public static final int CADAVA_BERRIES        = 753;
    public static final int GHOSTSPEAK_AMULET     = 552;
    public static final int SKULL_GHOST           = 553;
    public static final int FEATHER_ERNEST        = 296;     // Ernest's feather (poultry)
    public static final int OIL_CAN               = 287;
    public static final int RUBBER_TUBE           = 290;
    public static final int PRESSURE_GAUGE        = 289;
    public static final int FISHBOWL_ERNEST       = 285;
    public static final int FISH                  = 286;
    public static final int KEY_NORMAL            = 297;
    public static final int KEY_BLUE              = 298;
    public static final int KEY_RED               = 299;
    public static final int SHEARS                = 1735;
    public static final int WOOL                  = 1737;
    public static final int BALL_OF_WOOL          = 1759;
    public static final int LETTER_VARROCK_TO_DRAYNOR = 290; // overlaps; see Ernest
    public static final int GARLIC                = 1550;
    public static final int STAKE                 = 1549;
    public static final int STAKE_HAMMER          = 1539;
    public static final int IMP_BLUE              = 1438;     // air talisman drop  (re-used; placeholder)
    public static final int RED_BEAD              = 1470;
    public static final int YELLOW_BEAD           = 1472;
    public static final int BLACK_BEAD            = 1474;
    public static final int WHITE_BEAD            = 1476;
    public static final int EYE_OF_NEWT           = 200;
    public static final int RATS_TAIL             = 300;
    public static final int BURNT_MEAT            = 2146;
    public static final int ONION                 = 1957;
    public static final int LETTER                = 1602;
    public static final int CANDLE                = 36;
    public static final int CANDLE_BLACK          = 38;

    // ── Crafting / spinning ─────────────────────────────────────────────────
    public static final int FLAX                  = 1779;
    public static final int BOWSTRING             = 1777;
    public static final int COWHIDE               = 1739;
    public static final int SOFT_LEATHER          = 1741;
    public static final int HARD_LEATHER          = 1743;
}
