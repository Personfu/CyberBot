package nezz.dreambot.aio.boss.mechanic;

import nezz.dreambot.aio.boss.BossConfig;
import nezz.dreambot.aio.combat.CombatManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;

/**
 * Handles the Deranged Archaeologist's "Learn to read!" special: a book is
 * thrown that deals heavy damage to the tile you're standing on. The counter is
 * simply to not be standing on that tile when it lands - i.e. step one tile.
 *
 * Detection: the special fires on a regular ~10s cadence, so we proactively
 * reposition once per cycle. This reliably avoids the stationary book AoE
 * without depending on overhead-text APIs that vary between client builds. If
 * your build exposes the boss's overhead text or the cast animation id, plug it
 * into {@link #castImminent(NPC)} to tighten the timing further.
 */
public class DerangedArchaeologistMechanic extends BossMechanic {

	private static final long SPECIAL_CYCLE_MS = 9500; // ~ every 10s in-game
	private long lastDodge = 0;

	public DerangedArchaeologistMechanic(BossConfig boss, CombatManager combat) {
		super(boss, combat);
	}

	@Override
	public int handle() {
		NPC arch = combat.findByName(boss.npcName);
		if (arch == null) return NOT_HANDLED;

		long now = System.currentTimeMillis();
		if ((castImminent(arch) || now - lastDodge >= SPECIAL_CYCLE_MS) && dodge()) {
			lastDodge = now;
			return Calculations.random(450, 750);
		}
		return NOT_HANDLED;
	}

	/**
	 * Extension point for precise detection of the "Learn to read!" cast (e.g.
	 * via the boss's cast animation id on your server). Defaults to false so the
	 * timer cadence drives the dodge.
	 */
	private boolean castImminent(NPC arch) {
		return false;
	}

	/** Steps one tile off the current position. @return true if a move was issued. */
	private boolean dodge() {
		Player local = Players.getLocal();
		if (local == null) return false;
		Tile t = local.getTile();
		if (t == null) return false;
		// Pick a random adjacent tile to break stationary AoE.
		int[][] deltas = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, -1}};
		int[] d = deltas[Calculations.random(0, deltas.length - 1)];
		Tile dest = t.derive(d[0], d[1]);
		Logger.log("[DerangedArch] Dodging Learn to read! -> stepping aside.");
		if (Walking.walk(dest)) {
			Sleep.sleep(Calculations.random(250, 450));
			return true;
		}
		return false;
	}
}
