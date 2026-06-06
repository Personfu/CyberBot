package nezz.dreambot.aio.boss.mechanic;

import nezz.dreambot.aio.boss.BossConfig;
import nezz.dreambot.aio.combat.CombatManager;
import nezz.dreambot.aio.gui.Config;

/**
 * Maps a selected boss to its mechanic handler. Bosses without bespoke
 * mechanics get {@link NoMechanic}.
 */
public final class Mechanics {

	private Mechanics() {}

	public static BossMechanic forBoss(Config cfg, BossConfig boss, CombatManager combat) {
		switch (cfg.boss) {
			case SCURRIUS:
				return new ScurriusMechanic(boss, combat);
			case DERANGED_ARCHAEOLOGIST:
				return new DerangedArchaeologistMechanic(boss, combat);
			default:
				return new NoMechanic(boss, combat);
		}
	}
}
