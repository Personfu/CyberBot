package nezz.dreambot.aio.boss.mechanic;

import nezz.dreambot.aio.boss.BossConfig;
import nezz.dreambot.aio.combat.CombatManager;

/**
 * Bryophyta spawns "Growthling" minions that heal her if left alive, so they
 * must be cleared before damage on the boss counts. Reuses the generic
 * minion-priority handler.
 */
public class BryophytaMechanic extends MinionPriorityMechanic {

	public BryophytaMechanic(BossConfig boss, CombatManager combat) {
		super(boss, combat, "Bryophyta", null, "growthling");
	}
}
