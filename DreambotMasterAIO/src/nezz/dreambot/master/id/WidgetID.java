package nezz.dreambot.master.id;

/**
 * Curated widget (interface) IDs. Use DreamBot's {@code Widgets.getWidget(id)}
 * to access. Full canonical map:
 * <a href="https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/widgets/WidgetID.java">
 *   runelite-api/widgets/WidgetID.java</a>.
 *
 * <p>OSRS interface IDs are split into "group" (high 16 bits) and "child"
 * (low 16 bits) — DreamBot uses the group as the primary lookup so the
 * values stored here are bare group IDs.</p>
 */
public final class WidgetID {

    private WidgetID() { }

    public static final int LOGIN_CLICK_TO_PLAY      = 378;
    public static final int FIXED_VIEWPORT           = 548;
    public static final int RESIZABLE_VIEWPORT       = 161;
    public static final int RESIZABLE_VIEWPORT_BOTTOM_LINE = 164;

    public static final int CHATBOX                  = 162;
    public static final int DIALOGUE_PLAYER          = 217;
    public static final int DIALOGUE_NPC             = 231;
    public static final int DIALOGUE_OPTIONS         = 219;
    public static final int DIALOGUE_SPRITE          = 193;
    public static final int DIALOGUE_NOTIFICATION    = 229;
    public static final int LEVEL_UP                 = 233;

    public static final int INVENTORY                = 149;
    public static final int EQUIPMENT                = 387;
    public static final int BANK                     = 12;
    public static final int BANK_INVENTORY           = 15;
    public static final int DEPOSIT_BOX              = 192;
    public static final int GRAND_EXCHANGE           = 465;
    public static final int GRAND_EXCHANGE_OFFER     = 467;
    public static final int GE_HISTORY               = 383;

    public static final int SKILLS_TAB               = 320;
    public static final int QUEST_TAB                = 399;
    public static final int PRAYER_TAB               = 541;
    public static final int MAGIC_TAB                = 218;
    public static final int LOGOUT_TAB               = 182;
    public static final int FRIENDS_TAB              = 429;
    public static final int IGNORE_TAB               = 432;
    public static final int CLAN_TAB                 = 433;

    public static final int APPEARANCE_INTERFACE     = 269;
    public static final int TUTORIAL_OVERLAY         = 263;
    public static final int POLL_BOOTH               = 310;
    public static final int POLL_BOOTH_RESULT        = 345;

    public static final int MINIMAP                  = 160;
    public static final int WORLD_MAP                = 595;

    public static final int CHARACTER_SUMMARY        = 84;
    public static final int SMITHING                 = 312;
    public static final int CRAFTING                 = 270;
    public static final int FLETCHING                = 270;     // shares with crafting
    public static final int HERBLORE                 = 270;
    public static final int COOKING                  = 270;
    public static final int FAIRY_RING_PANEL         = 398;
    public static final int FAIRY_RING_CODE          = 397;

    public static final int RIGHT_CLICK              = 884;
    public static final int CHATBOX_INPUT            = 162;
    public static final int ENTER_AMOUNT             = 162;     // child 559
    public static final int MAKE_X                   = 270;

    public static final int CLICK_HERE_TO_CONTINUE_ID = 137;    // commonly skipped parent in TutIsland
}
