package nezz.dreambot.aio.boss;

import org.dreambot.api.methods.map.Tile;

/**
 * Server-agnostic description of a boss. Bosses are targeted by NPC name (so no
 * server-specific numeric IDs are required), with an anchor tile used to walk
 * to the arena when the boss isn't currently loaded. Adjust the anchor tiles to
 * match the CyberScape 317 server if they differ.
 */
public class BossConfig {

	public final String displayName;
	public final String npcName;
	public final Tile anchorTile;
	public final Tile bankTile;

	public BossConfig(String displayName, String npcName, Tile anchorTile, Tile bankTile) {
		this.displayName = displayName;
		this.npcName = npcName;
		this.anchorTile = anchorTile;
		this.bankTile = bankTile;
	}
}
