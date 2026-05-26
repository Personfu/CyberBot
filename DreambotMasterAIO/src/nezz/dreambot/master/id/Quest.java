package nezz.dreambot.master.id;

/**
 * Quest completion varps + their member-only flag. Matches the layout of
 * {@code net.runelite.api.Quest} so a porting tool can extend it directly.
 *
 * <p>Each enum value carries the varp ID, the value indicating "complete"
 * (varies per quest — sometimes a single flag, often a stage), and whether
 * it's F2P-accessible.</p>
 *
 * <p>Reference: <a href="https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Quest.java">
 *   runelite-api/Quest.java</a></p>
 */
public enum Quest {

    // ── F2P ──────────────────────────────────────────────────────────────────
    COOKS_ASSISTANT      ("Cook's Assistant",       29,   2, false),
    DEMON_SLAYER         ("Demon Slayer",           222,  3, false),
    DORICS_QUEST         ("Doric's Quest",          31,   100, false),
    DRAGON_SLAYER        ("Dragon Slayer I",        176,  10, false),
    ERNEST_THE_CHICKEN   ("Ernest the Chicken",     32,   3, false),
    GOBLIN_DIPLOMACY     ("Goblin Diplomacy",       130,  100, false),
    IMP_CATCHER          ("Imp Catcher",            160,  2, false),
    THE_KNIGHTS_SWORD    ("The Knight's Sword",     122,  6, false),
    PIRATES_TREASURE     ("Pirate's Treasure",      71,   4, false),
    PRINCE_ALI_RESCUE    ("Prince Ali Rescue",      273,  110, false),
    THE_RESTLESS_GHOST   ("The Restless Ghost",     107,  5, false),
    ROMEO_AND_JULIET     ("Romeo & Juliet",         144,  100, false),
    SHEEP_SHEARER        ("Sheep Shearer",          179,  21, false),
    VAMPYRE_SLAYER       ("Vampyre Slayer",         178,  3, false),
    WITCHES_POTION       ("Witch's Potion",         67,   3, false),
    BELOW_ICE_MOUNTAIN   ("Below Ice Mountain",     11103, 9, false),
    MISTHALIN_MYSTERY    ("Misthalin Mystery",      6557, 70, false),
    X_MARKS_THE_SPOT     ("X Marks the Spot",       8063, 7, false),
    SHIELD_OF_ARRAV      ("Shield of Arrav",        145,  7, false),
    RUNE_MYSTERIES       ("Rune Mysteries",         63,   6, false),

    // ── P2P (just the popular early ones for now) ────────────────────────────
    WATERFALL_QUEST      ("Waterfall Quest",        65,   10, true),
    TREE_GNOME_VILLAGE   ("Tree Gnome Village",     111,  50, true),
    FAIRY_TALE_PART_I    ("Fairytale I - Growing Pains", 9583, 90, true),
    LOST_CITY            ("Lost City",              147,  6, true),
    MONKS_FRIEND         ("Monk's Friend",          30,   2, true),
    PRIEST_IN_PERIL      ("Priest in Peril",        302,  60, true),
    JUNGLE_POTION        ("Jungle Potion",          175,  4, true),
    DRUIDIC_RITUAL       ("Druidic Ritual",         80,   4, true),
    BIOHAZARD            ("Biohazard",              68,   9, true),
    HOLY_GRAIL           ("Holy Grail",             76,   80, true);

    public final String name;
    public final int    varp;
    public final int    completeValue;
    public final boolean members;

    Quest(String name, int varp, int completeValue, boolean members) {
        this.name = name;
        this.varp = varp;
        this.completeValue = completeValue;
        this.members = members;
    }

    public static Quest byName(String s) {
        for (Quest q : values()) if (q.name.equalsIgnoreCase(s)) return q;
        return null;
    }
}
