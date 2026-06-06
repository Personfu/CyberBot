package nezz.dreambot.aio.boss;

import nezz.dreambot.aio.gui.Config;
import org.dreambot.api.methods.map.Tile;

/**
 * Built-in boss definitions. Anchor/bank tiles are best-effort for a 317 server
 * laid out like OSRS; if CyberScape uses different coordinates, override them
 * here or via the GUI name override. Targeting is name-based, so the fight loop
 * still works even if the anchor tile is approximate (it walks toward it, then
 * locks onto the named NPC once it loads).
 */
public final class BossRegistry {

	private BossRegistry() {}

	public static BossConfig forType(Config cfg) {
		String override = cfg.bossNameOverride != null ? cfg.bossNameOverride.trim() : "";
		Tile edgeville = new Tile(3094, 3491, 0);
		switch (cfg.boss) {
			case SEWER_AXEMAN:
				return new BossConfig(
						"Varrock Sewers (axe adds)",
						override.isEmpty() ? "Brutal" : override,
						// Varrock sewers, near the central chamber.
						new Tile(3237, 9858, 0), edgeville);
			case KING_BLACK_DRAGON:
				return new BossConfig(
						"King Black Dragon",
						override.isEmpty() ? "King Black Dragon" : override,
						new Tile(2271, 4680, 0), edgeville);
			case GIANT_MOLE:
				return new BossConfig(
						"Giant Mole",
						override.isEmpty() ? "Giant Mole" : override,
						new Tile(1752, 5237, 0),
						new Tile(3013, 3355, 0)); // Falador east
			case CHAOS_ELEMENTAL:
				return new BossConfig(
						"Chaos Elemental",
						override.isEmpty() ? "Chaos Elemental" : override,
						new Tile(3280, 3916, 0), edgeville);
			case IVAR_KING_OF_BONES:
				return new BossConfig(
						"Ivar, King of Bones",
						override.isEmpty() ? "Ivar, King of Bones" : override,
						new Tile(3257, 3673, 0), edgeville);
			case BRUTUS:
			default:
				return new BossConfig(
						"Brutus",
						override.isEmpty() ? "Brutus" : override,
						// Placeholder anchor - adjust to the server's Brutus arena.
						new Tile(3220, 3220, 0), edgeville);
		}
	}
}
