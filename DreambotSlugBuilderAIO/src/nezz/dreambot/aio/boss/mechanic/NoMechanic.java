package nezz.dreambot.aio.boss.mechanic;

import nezz.dreambot.aio.boss.BossConfig;
import nezz.dreambot.aio.combat.CombatManager;

/** Default no-op mechanic for bosses without special handling. */
public class NoMechanic extends BossMechanic {

	public NoMechanic(BossConfig boss, CombatManager combat) {
		super(boss, combat);
	}

	@Override
	public int handle() {
		return NOT_HANDLED;
	}
}
