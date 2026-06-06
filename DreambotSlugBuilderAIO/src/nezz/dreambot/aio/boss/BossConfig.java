package nezz.dreambot.aio.boss;

import nezz.dreambot.aio.prayer.PrayerManager;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;

/**
 * Server-agnostic description of a boss. Bosses are targeted by NPC name (so no
 * server-specific numeric IDs are required), with an anchor tile used to walk
 * to the arena when the boss isn't currently loaded. Also carries the combat
 * style hints the subsystems use: which overhead protection to keep up and
 * which offensive prayer family to enable.
 */
public class BossConfig {

	public final String displayName;
	public final String npcName;
	public final Tile anchorTile;
	public final Tile bankTile;

	/** Overhead protection to maintain during the fight. */
	public final PrayerManager.Protect protection;
	/** Offensive style (drives the offensive prayer + any boost potions). */
	public final Skill offensiveStyle;
	/** Whether the boss has adds worth attacking when the main target is gone. */
	public final boolean hasAdds;
	/**
	 * Optional game-object name the boss relocates to when it leaves (e.g. the
	 * Giant Mole's "Mole hills" after it burrows). If set, TRAVEL walks toward
	 * the nearest such object when the boss NPC isn't loaded. May be null.
	 */
	public final String relocateObjectName;

	public BossConfig(String displayName, String npcName, Tile anchorTile, Tile bankTile,
					  PrayerManager.Protect protection, Skill offensiveStyle, boolean hasAdds) {
		this(displayName, npcName, anchorTile, bankTile, protection, offensiveStyle, hasAdds, null);
	}

	public BossConfig(String displayName, String npcName, Tile anchorTile, Tile bankTile,
					  PrayerManager.Protect protection, Skill offensiveStyle, boolean hasAdds,
					  String relocateObjectName) {
		this.displayName = displayName;
		this.npcName = npcName;
		this.anchorTile = anchorTile;
		this.bankTile = bankTile;
		this.protection = protection;
		this.offensiveStyle = offensiveStyle;
		this.hasAdds = hasAdds;
		this.relocateObjectName = relocateObjectName;
	}
}
