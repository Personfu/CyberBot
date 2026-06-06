package nezz.dreambot.aio.boss.mechanic;

import nezz.dreambot.aio.boss.BossConfig;
import nezz.dreambot.aio.combat.CombatManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.Arrays;
import java.util.List;

/**
 * Generic "clear the minions first" mechanic. Bosses that spawn dangerous or
 * healing minions (Scurrius' rats, Bryophyta's growthlings) want those killed
 * before the boss is attacked. Detects attackable minions by name keyword and
 * prioritises the nearest one; yields when none remain so the boss is fought
 * normally.
 */
public class MinionPriorityMechanic extends BossMechanic {

	private final List<String> keywords;
	private final String excludeKeyword;
	private final String label;

	public MinionPriorityMechanic(BossConfig boss, CombatManager combat,
								  String label, String excludeKeyword, String... keywords) {
		super(boss, combat);
		this.label = label;
		this.excludeKeyword = excludeKeyword == null ? "" : excludeKeyword.toLowerCase();
		this.keywords = Arrays.asList(keywords);
	}

	@Override
	public int handle() {
		NPC minion = combat.find(n -> n != null
				&& isMinion(n.getName())
				&& n.hasAction("Attack")
				&& n.getHealthPercent() > 0
				&& n.distance() < 9);
		if (minion == null) {
			return NOT_HANDLED; // no minions -> attack the boss normally
		}
		NPC current = combat.currentTarget();
		if (current != null && isMinion(current.getName())) {
			return NOT_HANDLED; // already clearing a minion
		}
		Logger.log("[" + label + "] Prioritising minion: " + minion.getName());
		if (combat.attack(minion)) {
			return Calculations.random(350, 650);
		}
		return NOT_HANDLED;
	}

	private boolean isMinion(String name) {
		if (name == null) return false;
		String l = name.toLowerCase();
		if (!excludeKeyword.isEmpty() && l.contains(excludeKeyword)) return false;
		for (String k : keywords) {
			if (l.contains(k.toLowerCase())) return true;
		}
		return false;
	}
}
