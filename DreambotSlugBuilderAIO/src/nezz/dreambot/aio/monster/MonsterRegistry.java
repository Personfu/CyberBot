package nezz.dreambot.aio.monster;

import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.prayer.PrayerManager.Protect;
import org.dreambot.api.methods.map.Tile;

/**
 * Built-in monster grinds. Coordinates assume an OSRS-style 317 layout; adjust
 * anchor/safespot/bank tiles if CyberScape differs, or use the GUI name
 * override. Loot lists carry the genuinely valuable F2P drops for each target.
 */
public final class MonsterRegistry {

	private MonsterRegistry() {}

	public static MonsterConfig forType(Config cfg) {
		String override = cfg.monsterNameOverride != null ? cfg.monsterNameOverride.trim() : "";
		Tile edgeville = new Tile(3094, 3491, 0);
		switch (cfg.monster) {
			case CHAOS_DRUIDS:
				// Edgeville dungeon; drop grimy herbs - top F2P-RSPS herb income.
				return new MonsterConfig("Chaos Druids",
						name(override, "Chaos druid"),
						new Tile(3097, 9850, 0), edgeville, null, Protect.NONE,
						"Grimy ranarr weed", "Grimy snapdragon", "Grimy torstol",
						"Grimy avantoe", "Grimy kwuarm", "Grimy lantadyme",
						"Grimy cadantine", "Grimy dwarf weed", "Limpwurt root");
			case GREEN_DRAGONS:
				// Wilderness greens; bones + hides. Bring antifire/anti-dragon shield.
				return new MonsterConfig("Green Dragons",
						name(override, "Green dragon"),
						new Tile(3349, 3673, 0), edgeville, null, Protect.NONE,
						"Dragon bones", "Green dragonhide");
			case MOSS_GIANTS:
				return new MonsterConfig("Moss Giants",
						name(override, "Moss giant"),
						new Tile(3157, 9906, 0), edgeville, null, Protect.NONE,
						"Big bones", "Mossy key", "Black key", "Limpwurt root");
			case LESSER_DEMONS:
				// Karamja volcano / Wizards' Tower top; classic magic safespot.
				return new MonsterConfig("Lesser Demons (safespot)",
						name(override, "Lesser demon"),
						new Tile(2843, 9573, 0), edgeville,
						new Tile(2841, 9569, 0), Protect.NONE,
						"Rune medium helm", "Ensouled imp head");
			case HILL_GIANTS:
			default:
				// Edgeville dungeon hill giants; big bones + limpwurt roots.
				return new MonsterConfig("Hill Giants",
						name(override, "Hill giant"),
						new Tile(3117, 9852, 0), edgeville, null, Protect.NONE,
						"Big bones", "Limpwurt root", "Giant key");
		}
	}

	private static String name(String override, String def) {
		return override.isEmpty() ? def : override;
	}
}
