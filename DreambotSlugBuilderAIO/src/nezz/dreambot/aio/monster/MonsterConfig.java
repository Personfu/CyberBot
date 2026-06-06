package nezz.dreambot.aio.monster;

import nezz.dreambot.aio.prayer.PrayerManager;
import org.dreambot.api.methods.map.Tile;

import java.util.Arrays;
import java.util.List;

/**
 * Server-agnostic description of a regular monster grind (training / money).
 * Targeted by NPC name; an anchor tile walks us to the spot and an optional
 * safespot tile lets ranged/magic farm without taking melee. Loot is a mix of
 * a named drop list (always taken) plus a GE value threshold.
 */
public class MonsterConfig {

	public final String displayName;
	public final String npcName;
	public final Tile anchorTile;
	public final Tile bankTile;
	public final Tile safespotTile;        // null = no safespot
	public final PrayerManager.Protect protection;
	public final List<String> lootNames;   // always picked up

	public MonsterConfig(String displayName, String npcName, Tile anchorTile, Tile bankTile,
						 Tile safespotTile, PrayerManager.Protect protection, String... lootNames) {
		this.displayName = displayName;
		this.npcName = npcName;
		this.anchorTile = anchorTile;
		this.bankTile = bankTile;
		this.safespotTile = safespotTile;
		this.protection = protection;
		this.lootNames = Arrays.asList(lootNames);
	}

	public boolean hasSafespot() {
		return safespotTile != null;
	}
}
