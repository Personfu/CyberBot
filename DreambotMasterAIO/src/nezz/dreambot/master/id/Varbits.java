package nezz.dreambot.master.id;

/**
 * Curated subset of RuneLite's {@code Varbits} constants — the values needed
 * for the MasterAIO's tutorial automation, F2P quest engine, and runtime
 * state checks. The full list lives at
 * <a href="https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Varbits.java">
 *   runelite-api/net/runelite/api/Varbits.java
 * </a>; values here are kept name-compatible with that file so a porting tool
 * can extend it without renames.
 *
 * <p>Constants are ints (not enums) on purpose — DreamBot's
 * {@code PlayerSettings.getBitValue(int)} takes an int and these values are
 * passed by reference all over quest stage tables.</p>
 */
public final class Varbits {

    private Varbits() { }

    // ── Account / general ────────────────────────────────────────────────────
    public static final int ACCOUNT_TYPE          = 1777;   // 0=normal, 1=iron, 2=ult, 3=hcim, 4=gim
    public static final int IN_RAID               = 5424;
    public static final int IN_WILDERNESS         = 5963;
    public static final int MEMBERSHIP_DAYS       = 1620;
    public static final int IS_HIDDEN_MEMBER      = 4071;
    public static final int BANK_REARRANGE_MODE   = 3959;

    // ── Tutorial Island progress (varp 281) is read via PlayerSettings.getConfig,
    //    but the per-section flags also live in varbits for fine-grained checks.
    public static final int TUTORIAL_PROGRESS     = 281;    // primary tutorial varp
    public static final int TUTORIAL_TAB_CONFIG   = 1021;

    // ── Run / energy ─────────────────────────────────────────────────────────
    public static final int RUN_ENERGY            = 173;
    public static final int STAMINA_EFFECT        = 25;

    // ── Prayer / spellbook ───────────────────────────────────────────────────
    public static final int SPELLBOOK             = 4070;   // 0=Normal,1=Ancients,2=Lunar,3=Arceuus
    public static final int QUICK_PRAYER          = 4103;

    // ── Combat / loot ────────────────────────────────────────────────────────
    public static final int IN_COMBAT             = 8133;
    public static final int LOOT_DROPS_ACCOUNT_VALUE = 5980;

    // ── GE ───────────────────────────────────────────────────────────────────
    public static final int GE_SLOT_1_STATUS      = 4439;
    public static final int GE_SLOT_2_STATUS      = 4445;
    public static final int GE_SLOT_3_STATUS      = 4451;
    public static final int GE_SLOT_4_STATUS      = 4457;
    public static final int GE_SLOT_5_STATUS      = 4463;
    public static final int GE_SLOT_6_STATUS      = 4469;
    public static final int GE_SLOT_7_STATUS      = 4475;
    public static final int GE_SLOT_8_STATUS      = 4481;

    // ── Slayer ───────────────────────────────────────────────────────────────
    public static final int SLAYER_ASSIGNMENT     = 2502;
    public static final int SLAYER_COUNT          = 2503;
    public static final int SLAYER_POINTS         = 4068;
    public static final int SLAYER_TASK_STREAK    = 4069;

    // ── Diary completion shortcuts (set when each diary tier finishes) ───────
    public static final int DIARY_LUMBRIDGE_EASY     = 4351;
    public static final int DIARY_LUMBRIDGE_MEDIUM   = 4352;
    public static final int DIARY_LUMBRIDGE_HARD     = 4353;
    public static final int DIARY_LUMBRIDGE_ELITE    = 4354;
    public static final int DIARY_VARROCK_EASY       = 4515;
    public static final int DIARY_VARROCK_MEDIUM     = 4516;
    public static final int DIARY_VARROCK_HARD       = 4517;
    public static final int DIARY_VARROCK_ELITE      = 4518;
    public static final int DIARY_FALADOR_EASY       = 4503;
    public static final int DIARY_FALADOR_MEDIUM     = 4504;
    public static final int DIARY_FALADOR_HARD       = 4505;
    public static final int DIARY_FALADOR_ELITE      = 4506;

    // ── F2P quest stage varbits (mostly from quest-helper/QuestVarbits.java) ─
    public static final int QUEST_COOKS_ASSISTANT      = 29;
    public static final int QUEST_ROMEO_JULIET         = 144;
    public static final int QUEST_SHEEP_SHEARER        = 179;
    public static final int QUEST_GOBLIN_DIPLOMACY     = 130;
    public static final int QUEST_IMP_CATCHER          = 8;
    public static final int QUEST_WITCHES_POTION       = 67;
    public static final int QUEST_RESTLESS_GHOST       = 107;
    public static final int QUEST_VAMPYRE_SLAYER       = 178;
    public static final int QUEST_ERNEST_THE_CHICKEN   = 32;
    public static final int QUEST_DORICS_QUEST         = 31;
    public static final int QUEST_PRINCE_ALI_RESCUE    = 273;
    public static final int QUEST_KNIGHTS_SWORD        = 122;
    public static final int QUEST_DRAGON_SLAYER        = 176;
    public static final int QUEST_LOST_CITY            = 147;
    public static final int QUEST_MISTHALIN_MYSTERY    = 6557;
    public static final int QUEST_X_MARKS_THE_SPOT     = 8063;
    public static final int QUEST_BELOW_ICE_MOUNTAIN   = 11103;
    // These use varp (getConfig) not getBitValue
    public static final int QUEST_DEMON_SLAYER         = 18;   // varp
    public static final int QUEST_BLACK_KNIGHTS_FORTRESS = 176; // varp (complete=1)
    public static final int QUEST_RUNE_MYSTERIES       = 63;   // varp
    public static final int QUEST_SHIELD_OF_ARRAV      = 73;   // varp
    public static final int QUEST_PIRATES_TREASURE     = 71;   // varp
    public static final int QUEST_CORSAIR_CURSE        = 367;  // varp

    // ── Common world state checks ────────────────────────────────────────────
    public static final int POLL_BOOTH_ACTIVE = 375;
    public static final int APPEARANCE_INTERFACE_OPEN = 269;
    public static final int ATTACK_SPEED      = 843;
    public static final int PERCENT_COMPLETE  = 406;
}
