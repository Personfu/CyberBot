package nezz.dreambot.allinone.config;

import java.util.*;

/**
 * Hardcoded OSRS drop tables for common combat NPCs.
 * Sources: OSRS Wiki (rates are approximate where weighted tables are involved).
 *
 * Each entry uses real OSRS item IDs so DreamBot can match ground items by ID.
 */
public final class NpcLootProfile {

    private NpcLootProfile() {}

    // ── Known NPC names (used to populate combo-boxes) ───────────────────────

    public static final String[] KNOWN_NPCS = {
        "Chicken", "Cow", "Goblin", "Hill Giant", "Moss Giant",
        "Lesser Demon", "Greater Demon", "Black Demon",
        "Abyssal Demon", "Gargoyle",
        "Dagannoth Rex", "Dagannoth Supreme", "Dagannoth Prime",
        "General Graardor", "Cerberus",
        "King Black Dragon", "Vorkath", "Zulrah"
    };

    /** Returns all loot entries for the given NPC name (case-insensitive). */
    public static List<LootEntry> getDropTable(String npcName) {
        if (npcName == null) return Collections.emptyList();
        switch (npcName.trim().toLowerCase()) {
            case "chicken":         return chicken();
            case "cow":             return cow();
            case "goblin":          return goblin();
            case "hill giant":      return hillGiant();
            case "moss giant":      return mossGiant();
            case "lesser demon":    return lesserDemon();
            case "greater demon":   return greaterDemon();
            case "black demon":     return blackDemon();
            case "abyssal demon":   return abyssalDemon();
            case "gargoyle":        return gargoyle();
            case "dagannoth rex":   return dagannothRex();
            case "dagannoth supreme": return dagannothSupreme();
            case "dagannoth prime": return dagannothPrime();
            case "general graardor": return generalGraardor();
            case "cerberus":        return cerberus();
            case "king black dragon": return kingBlackDragon();
            case "vorkath":         return vorkath();
            case "zulrah":          return zulrah();
            default:                return Collections.emptyList();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NPC drop tables
    // ─────────────────────────────────────────────────────────────────────────

    private static List<LootEntry> chicken() {
        return Arrays.asList(
            LootEntry.always("Bones",       526,  1),
            LootEntry.always("Raw chicken", 2138, 1),
            LootEntry.alwaysRange("Feathers", 314, 5, 15)
        );
    }

    private static List<LootEntry> cow() {
        return Arrays.asList(
            LootEntry.always("Bones",    526,  1),
            LootEntry.always("Cowhide",  1739, 1),
            LootEntry.always("Raw beef", 2132, 1)
        );
    }

    private static List<LootEntry> goblin() {
        return Arrays.asList(
            LootEntry.always("Bones",    526, 1),
            LootEntry.weighted("Coins",  995, 1,  5,  50, 128),
            LootEntry.rate("Goblin mail", 1101, 1, 8),
            LootEntry.rate("Clue scroll (beginner)", 23182, 1, 100)
        );
    }

    private static List<LootEntry> hillGiant() {
        return Arrays.asList(
            LootEntry.always("Big bones",  532, 1),
            LootEntry.weighted("Coins",    995, 15, 25, 25, 128),
            LootEntry.rate("Limpwurt root", 225, 1, 15),
            LootEntry.rate("Giant key",   2986, 1, 128),
            LootEntry.rate("Rune 2h sword", 1319, 1, 512),
            LootEntry.rate("Clue scroll (medium)", 2801, 1, 128)
        );
    }

    private static List<LootEntry> mossGiant() {
        return Arrays.asList(
            LootEntry.always("Big bones",  532, 1),
            LootEntry.weighted("Coins",    995, 50, 100, 30, 128),
            LootEntry.rate("Grimy ranarr weed",   207, 1, 30),
            LootEntry.rate("Grimy irit leaf",      209, 1, 35),
            LootEntry.rate("Grimy avantoe",        211, 1, 50),
            LootEntry.rate("Mossy key",         22997, 1, 128),
            LootEntry.rate("Rune med helm",      1149, 1, 256),
            LootEntry.rate("Clue scroll (medium)", 2801, 1, 128)
        );
    }

    private static List<LootEntry> lesserDemon() {
        return Arrays.asList(
            LootEntry.always("Ashes",         592, 1),
            LootEntry.weighted("Coins",        995, 100, 450, 55, 256),
            LootEntry.weighted("Chaos rune",   562,  10,  20, 20, 256),
            LootEntry.weighted("Fire rune",    554,  15,  30, 15, 256),
            LootEntry.weighted("Steel platebody", 1119, 1, 1, 10, 256),
            LootEntry.rate("Rune med helm",   1149, 1, 128),
            LootEntry.rate("Clue scroll (medium)", 2801, 1, 512)
        );
    }

    private static List<LootEntry> greaterDemon() {
        return Arrays.asList(
            LootEntry.always("Ashes",           592, 1),
            LootEntry.weighted("Coins",          995, 300, 900, 40, 256),
            LootEntry.weighted("Chaos rune",     562,  15,  30, 20, 256),
            LootEntry.weighted("Fire rune",      554,  25,  50, 15, 256),
            LootEntry.weighted("Rune chainbody", 1109,  1,   1,  8, 256),
            LootEntry.rate("Rune full helm",    1163,  1, 128),
            LootEntry.rate("Rune 2h sword",     1319,  1, 512),
            LootEntry.rate("Clue scroll (hard)", 2722, 1, 128)
        );
    }

    private static List<LootEntry> blackDemon() {
        return Arrays.asList(
            LootEntry.always("Ashes",              592, 1),
            LootEntry.weighted("Coins",             995, 800, 2000, 35, 256),
            LootEntry.weighted("Rune full helm",   1163,   1,    1, 10, 256),
            LootEntry.weighted("Rune chainbody",   1109,   1,    1,  8, 256),
            LootEntry.rate("Rune 2h sword",        1319, 1, 128),
            LootEntry.rate("Clue scroll (hard)",   2722, 1,  64),
            LootEntry.rate("Shield left half",     2366, 1, 512)
        );
    }

    private static List<LootEntry> abyssalDemon() {
        return Arrays.asList(
            LootEntry.always("Ashes",                 592, 1),
            LootEntry.weighted("Coins",                995, 980, 1000, 24, 128),
            LootEntry.weighted("Death rune",           560,  20,   20,  8, 128),
            LootEntry.weighted("Blood rune",           565,   7,    7,  7, 128),
            LootEntry.weighted("Chaos rune",           562,  30,   30,  7, 128),
            LootEntry.weighted("Adamant platelegs",   3887,   1,    1,  5, 128),
            LootEntry.weighted("Rune chainbody",      1109,   1,    1,  5, 128),
            LootEntry.weighted("Rune med helm",       1149,   1,    1,  4, 128),
            LootEntry.weighted("Noted adamant bar",   2361,   4,    4,  3, 128),
            LootEntry.weighted("Noted runite bar",    2364,   1,    1,  2, 128),
            LootEntry.weighted("Shield left half",    2366,   1,    1,  1, 128),
            LootEntry.rate("Ensouled abyssal head", 13508,   1,  25),
            LootEntry.rate("Abyssal whip",           4151,   1, 512),
            LootEntry.rate("Abyssal dagger",        13265,   1, 32768),
            LootEntry.rate("Clue scroll (elite)",   12073,   1, 512)
        );
    }

    private static List<LootEntry> gargoyle() {
        // Weighted table total = 256 (from drop_tables.yml in this repo)
        return Arrays.asList(
            LootEntry.always("Bones",              526,   1),
            LootEntry.weighted("Coins",             995, 400,  2000, 80, 256),
            LootEntry.weighted("Coins",             995, 3000, 10000, 22, 256),
            LootEntry.weighted("Pure essence",     7936,  25,  100, 36, 256),
            LootEntry.weighted("Mithril ore",       447,   5,   15, 30, 256),
            LootEntry.weighted("Mossy key",       22997,   1,    1, 50, 256),
            LootEntry.weighted("Steel battleaxe",  2963,   1,    1, 12, 256),
            LootEntry.weighted("Mithril kiteshield", 1197, 1,   1, 10, 256),
            LootEntry.weighted("Rune dagger",      1249,   1,    1,  6, 256),
            LootEntry.weighted("Adamant 2h sword", 1313,   1,    1,  5, 256),
            LootEntry.weighted("Mithril boots",    9921,   1,    1,  4, 256),
            LootEntry.weighted("Granite maul",     4153,   1,    1,  1, 256)  // rare
        );
    }

    private static List<LootEntry> dagannothRex() {
        return Arrays.asList(
            LootEntry.always("Big bones",   532, 1),
            LootEntry.weighted("Coins",     995, 10000, 25000, 20, 128),
            LootEntry.weighted("Grimy snapdragon", 3000, 1, 3, 15, 128),
            LootEntry.weighted("Grimy torstol",    219, 1, 3, 10, 128),
            LootEntry.weighted("Rune axe",        1351, 1, 1, 10, 128),
            LootEntry.weighted("Rune 2h sword",   1319, 1, 1,  5, 128),
            LootEntry.rate("Dragon axe",          6739, 1, 128),
            LootEntry.rate("Berserker ring",      6737, 1, 128),
            LootEntry.rate("Mud battlestaff",     6562, 1, 128),
            LootEntry.rate("Clue scroll (elite)", 12073, 1, 64)
        );
    }

    private static List<LootEntry> dagannothSupreme() {
        return Arrays.asList(
            LootEntry.always("Big bones",   532, 1),
            LootEntry.weighted("Coins",     995, 10000, 25000, 20, 128),
            LootEntry.weighted("Rune crossbow", 9185, 1, 1, 8, 128),
            LootEntry.rate("Dragon axe",    6739, 1, 128),
            LootEntry.rate("Archers ring",  6733, 1, 128),
            LootEntry.rate("Seercull",      6724, 1, 128),
            LootEntry.rate("Clue scroll (elite)", 12073, 1, 64)
        );
    }

    private static List<LootEntry> dagannothPrime() {
        return Arrays.asList(
            LootEntry.always("Big bones",   532, 1),
            LootEntry.weighted("Coins",     995, 10000, 25000, 20, 128),
            LootEntry.weighted("Mystic hat (light)", 4109, 1, 1, 8, 128),
            LootEntry.rate("Dragon axe",    6739, 1, 128),
            LootEntry.rate("Seers ring",    6731, 1, 128),
            LootEntry.rate("Clue scroll (elite)", 12073, 1, 64)
        );
    }

    private static List<LootEntry> generalGraardor() {
        return Arrays.asList(
            LootEntry.always("Big bones",     532,   1),
            LootEntry.weighted("Coins",        995, 19500, 65000, 40, 128),
            LootEntry.weighted("Grimy torstol", 219,    1,    3, 20, 128),
            LootEntry.weighted("Cannonball",   2,    60,   100, 15, 128),
            LootEntry.weighted("Rune 2h sword", 1319,  1,    1, 10, 128),
            LootEntry.weighted("Rune kiteshield", 1199, 1,   1,  8, 128),
            LootEntry.rate("Bandos chestplate", 11832, 1, 381),
            LootEntry.rate("Bandos tassets",    11834, 1, 381),
            LootEntry.rate("Bandos boots",      11836, 1, 381),
            LootEntry.rate("Bandos hilt",       11814, 1, 508),
            LootEntry.rate("Godsword shard 1",  11818, 1, 762),
            LootEntry.rate("Godsword shard 2",  11820, 1, 762),
            LootEntry.rate("Godsword shard 3",  11822, 1, 762),
            LootEntry.rate("Clue scroll (elite)", 12073, 1, 64)
        );
    }

    private static List<LootEntry> cerberus() {
        return Arrays.asList(
            LootEntry.always("Big bones",     532,   1),
            LootEntry.weighted("Coins",        995, 10000, 15000, 30, 128),
            LootEntry.weighted("Grimy snapdragon", 3000, 1, 3, 20, 128),
            LootEntry.weighted("Grimy torstol",  219, 1, 3, 15, 128),
            LootEntry.weighted("Super restore (4)", 3025, 2, 4, 10, 128),
            LootEntry.weighted("Prayer potion (4)", 2434, 2, 4, 10, 128),
            LootEntry.rate("Clue scroll (elite)", 12073, 1, 100),
            LootEntry.rate("Primordial crystal", 13231, 1, 512),
            LootEntry.rate("Pegasian crystal",   13229, 1, 512),
            LootEntry.rate("Eternal crystal",    13227, 1, 512),
            LootEntry.rate("Smouldering stone",  13233, 1, 512),
            LootEntry.rate("Hellpuppy (pet)",    13247, 1, 512)
        );
    }

    private static List<LootEntry> kingBlackDragon() {
        return Arrays.asList(
            LootEntry.always("Dragon bones",    536,  1),
            LootEntry.alwaysRange("Black dragonhide", 1747, 2, 2),
            LootEntry.weighted("Coins",          995, 10, 550, 40, 256),
            LootEntry.weighted("Death rune",     560,  5,  15, 25, 256),
            LootEntry.weighted("Blood rune",     565,  5,  10, 20, 256),
            LootEntry.weighted("Dragon platelegs", 4087, 1, 1, 5, 256),
            LootEntry.weighted("Dragon plateskirt", 4585, 1, 1, 5, 256),
            LootEntry.rate("KBD heads",          2306, 1,  128),
            LootEntry.rate("Draconic visage",   11286, 1, 5000),
            LootEntry.rate("Clue scroll (elite)", 12073, 1, 64)
        );
    }

    private static List<LootEntry> vorkath() {
        return Arrays.asList(
            LootEntry.always("Big bones",             532,  1),
            LootEntry.alwaysRange("Super antifire (4)", 21978, 1, 2),
            LootEntry.always("Antidragon shield",     1540,  1),
            LootEntry.weighted("Coins",                995, 8001, 16000, 30, 256),
            LootEntry.weighted("Death rune",           560,    5,    25, 20, 256),
            LootEntry.weighted("Blood rune",           565,    5,    20, 15, 256),
            LootEntry.weighted("Noted dragon bones",   537,   10,    30, 12, 256),
            LootEntry.weighted("Noted magic log",     1515,   10,    30, 10, 256),
            LootEntry.weighted("Noted runite ore",    451,     2,     8,  8, 256),
            LootEntry.weighted("Noted shark",         387,     8,    20,  8, 256),
            LootEntry.rate("Dragonbone necklace",    22111, 1, 1000),
            LootEntry.rate("Skeletal visage",        22006, 1, 5000),
            LootEntry.rate("Vorki (pet)",            22004, 1, 3000),
            LootEntry.rate("Clue scroll (elite)",    12073, 1,   65)
        );
    }

    private static List<LootEntry> zulrah() {
        return Arrays.asList(
            LootEntry.alwaysRange("Zulrah's scales", 12934, 20, 387),
            LootEntry.rate("Uncut onyx",              6571, 1, 512),
            LootEntry.rate("Magic seed",              5316, 1, 172),
            LootEntry.rate("Palm tree seed",          5289, 1, 172),
            LootEntry.rate("Torstol seed",            5304, 1, 65),
            LootEntry.rate("Tanzanite fang",         12922, 1, 512),
            LootEntry.rate("Magic fang",             12932, 1, 512),
            LootEntry.rate("Serpentine visage",      12927, 1, 512),
            LootEntry.rate("Tanzanite mutagen",      12921, 1, 13106),
            LootEntry.rate("Magma mutagen",          12911, 1, 13106),
            LootEntry.rate("Pet snakeling",          12921, 1, 4000),
            LootEntry.rate("Clue scroll (elite)",    12073, 1,  75)
        );
    }
}
