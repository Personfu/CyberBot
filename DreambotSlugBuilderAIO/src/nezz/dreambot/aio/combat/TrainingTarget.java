package nezz.dreambot.aio.combat;

import org.dreambot.api.methods.map.Tile;

/**
 * A combat-training target: NPC name, location, level range it's suitable for,
 * and optional safespot tile.
 */
public class TrainingTarget {

	public final String displayName;
	public final String npcName;
	public final int minCombatLevel;
	public final int maxCombatLevel;
	public final Tile anchorTile;
	public final Tile bankTile;
	public final Tile safespotTile;
	public final String[] lootNames;

	public TrainingTarget(String displayName, String npcName,
						  int minCombatLevel, int maxCombatLevel,
						  Tile anchorTile, Tile bankTile, Tile safespotTile,
						  String... lootNames) {
		this.displayName = displayName;
		this.npcName = npcName;
		this.minCombatLevel = minCombatLevel;
		this.maxCombatLevel = maxCombatLevel;
		this.anchorTile = anchorTile;
		this.bankTile = bankTile;
		this.safespotTile = safespotTile;
		this.lootNames = lootNames;
	}

	public boolean hasSafespot() {
		return safespotTile != null;
	}
}
