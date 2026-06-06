package nezz.dreambot.aio.boss.mechanic;

import nezz.dreambot.aio.boss.BossConfig;
import nezz.dreambot.aio.combat.CombatManager;

/**
 * Per-boss mechanic hook. {@link #handle} runs once per fight tick, before the
 * generic attack logic, giving a boss a chance to react to a special attack,
 * prioritise minions, reposition, etc.
 *
 * Return {@link #NOT_HANDLED} to let the generic fight logic proceed, or a
 * non-negative sleep value (ms) to indicate the mechanic took control this tick
 * and the task should sleep that long before looping again.
 */
public abstract class BossMechanic {

	/** Sentinel: the mechanic did nothing this tick. */
	public static final int NOT_HANDLED = Integer.MIN_VALUE;

	protected final BossConfig boss;
	protected final CombatManager combat;

	protected BossMechanic(BossConfig boss, CombatManager combat) {
		this.boss = boss;
		this.combat = combat;
	}

	/** @return a non-negative sleep if handled, otherwise {@link #NOT_HANDLED}. */
	public abstract int handle();

	public static boolean handled(int result) {
		return result != NOT_HANDLED;
	}
}
