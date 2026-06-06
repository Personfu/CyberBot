package nezz.dreambot.aio.boss.mechanic;

import nezz.dreambot.aio.boss.BossConfig;
import nezz.dreambot.aio.combat.CombatManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Scurrius periodically spawns "Giant rat" minions. They hit hard if ignored
 * and Scurrius gains an attack-speed buff while they're alive, so the optimal
 * play is to clear the minions before resuming the boss. This mechanic detects
 * attackable rat minions near the player and prioritises them; when none remain
 * it yields to the generic logic so the boss is attacked normally.
 */
public class ScurriusMechanic extends BossMechanic {

	public ScurriusMechanic(BossConfig boss, CombatManager combat) {
		super(boss, combat);
	}

	@Override
	public int handle() {
		NPC minion = combat.find(n -> n != null
				&& n.getName() != null
				&& isMinion(n.getName())
				&& n.hasAction("Attack")
				&& n.getHealthPercent() > 0
				&& n.distance() < 9);
		if (minion == null) {
			return NOT_HANDLED; // no minions -> attack the boss normally
		}
		// Only re-target if we're not already on a minion.
		NPC current = combat.currentTarget();
		if (current != null && isMinion(safeName(current))) {
			return NOT_HANDLED; // already clearing a minion
		}
		Logger.log("[Scurrius] Prioritising minion: " + minion.getName());
		if (combat.attack(minion)) {
			return Calculations.random(350, 650);
		}
		return NOT_HANDLED;
	}

	private boolean isMinion(String name) {
		String l = name.toLowerCase();
		return l.contains("rat") && !l.contains("scurrius");
	}

	private String safeName(NPC n) {
		return n.getName() == null ? "" : n.getName();
	}
}
