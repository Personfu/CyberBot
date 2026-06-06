package nezz.dreambot.aio.boss;

import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.prayer.PrayerManager.Protect;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;

/**
 * Built-in boss definitions. Anchor/bank tiles are best-effort for a 317 server
 * laid out like OSRS; if CyberScape uses different coordinates, override them
 * here or via the GUI name override. Targeting is name-based, so the fight loop
 * still works even if the anchor tile is approximate.
 *
 * The protection prayer + offensive style reflect each boss's primary attack
 * type (e.g. KBD/Brutus melee -> Protect from Melee; Mole melee; Chaos
 * Elemental hits all styles so we default to Magic protection).
 */
public final class BossRegistry {

	private BossRegistry() {}

	public static BossConfig forType(Config cfg) {
		String override = cfg.bossNameOverride != null ? cfg.bossNameOverride.trim() : "";
		Tile edgeville = new Tile(3094, 3491, 0);
		switch (cfg.boss) {
			case SEWER_AXEMAN:
				return new BossConfig("Varrock Sewers (axe adds)",
						name(override, "Brutal"),
						new Tile(3237, 9858, 0), edgeville,
						Protect.MELEE, Skill.STRENGTH, true);
			case KING_BLACK_DRAGON:
				return new BossConfig("King Black Dragon",
						name(override, "King Black Dragon"),
						new Tile(2271, 4680, 0), edgeville,
						Protect.MAGIC, Skill.STRENGTH, false);
			case GIANT_MOLE:
				// The mole burrows and resurfaces at "Mole hills"; relocate to them.
				return new BossConfig("Giant Mole",
						name(override, "Giant Mole"),
						new Tile(1752, 5237, 0), new Tile(3013, 3355, 0),
						Protect.NONE, Skill.STRENGTH, false, "Mole hills");
			case CHAOS_ELEMENTAL:
				return new BossConfig("Chaos Elemental",
						name(override, "Chaos Elemental"),
						new Tile(3280, 3916, 0), edgeville,
						Protect.MAGIC, Skill.STRENGTH, false);
			case IVAR_KING_OF_BONES:
				return new BossConfig("Ivar, King of Bones",
						name(override, "Ivar, King of Bones"),
						new Tile(3257, 3673, 0), edgeville,
						Protect.MELEE, Skill.STRENGTH, true);
			case SCURRIUS:
				// Rat boss beneath Varrock Palace; squeaking rat minions.
				return new BossConfig("Scurrius",
						name(override, "Scurrius"),
						new Tile(3287, 9851, 0), edgeville,
						Protect.MELEE, Skill.STRENGTH, true);
			case OBOR:
				// Hill Giant boss, Edgeville Dungeon (Giant key).
				return new BossConfig("Obor",
						name(override, "Obor"),
						new Tile(3105, 9933, 0), edgeville,
						Protect.MELEE, Skill.STRENGTH, false);
			case BRYOPHYTA:
				// Moss Giant boss near Varrock entrance (Mossy key).
				return new BossConfig("Bryophyta",
						name(override, "Bryophyta"),
						new Tile(3170, 9882, 0), edgeville,
						Protect.MELEE, Skill.STRENGTH, false);
			case DERANGED_ARCHAEOLOGIST:
				// Fossil Island swamp; ranged attacker, "Learn to read!" AoE.
				return new BossConfig("Deranged Archaeologist",
						name(override, "Deranged Archaeologist"),
						new Tile(3686, 3717, 0), edgeville,
						Protect.RANGED, Skill.STRENGTH, false);
			case BRUTUS:
			default:
				return new BossConfig("Brutus",
						name(override, "Brutus"),
						new Tile(3220, 3220, 0), edgeville,
						Protect.MELEE, Skill.STRENGTH, false);
		}
	}

	private static String name(String override, String def) {
		return override.isEmpty() ? def : override;
	}
}
