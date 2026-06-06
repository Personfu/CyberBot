package nezz.dreambot.aio.boss.mechanic;

import nezz.dreambot.aio.boss.BossConfig;
import nezz.dreambot.aio.combat.CombatManager;

/**
 * Scurrius periodically spawns "Giant rat" minions that hit hard and buff the
 * boss while alive. Clear them first.
 */
public class ScurriusMechanic extends MinionPriorityMechanic {

	public ScurriusMechanic(BossConfig boss, CombatManager combat) {
		super(boss, combat, "Scurrius", "scurrius", "rat");
	}
}
