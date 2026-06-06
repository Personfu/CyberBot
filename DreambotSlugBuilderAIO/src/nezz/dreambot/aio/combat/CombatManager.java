package nezz.dreambot.aio.combat;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Character;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Target acquisition and attacking, plus special-attack management. Keeps the
 * player attacking a valid target and fires the spec when the energy bar is
 * full enough (useful for DDS/claws style burst on bosses).
 */
public class CombatManager {

	/** Returns the NPC we're currently fighting, or null. */
	public NPC currentTarget() {
		Character<?> c = Players.getLocal().getInteractingCharacter();
		if (c instanceof NPC && c.exists()) {
			return (NPC) c;
		}
		return null;
	}

	public boolean inCombat() {
		return Players.getLocal() != null && Players.getLocal().isInCombat();
	}

	/** Closest attackable NPC matching the name. */
	public NPC findByName(String name) {
		return NPCs.closest(n -> n != null && name.equals(n.getName())
				&& n.hasAction("Attack") && n.getHealthPercent() > 0);
	}

	/** Closest attackable NPC matching a custom filter. */
	public NPC find(Filter<NPC> filter) {
		return NPCs.closest(filter);
	}

	/**
	 * Attacks the target if we're not already fighting it.
	 * @return true if an attack interaction was issued.
	 */
	public boolean attack(NPC target) {
		if (target == null || !target.exists()) return false;
		NPC cur = currentTarget();
		if (cur != null && cur.equals(target)) return false;
		if (target.interact("Attack")) {
			final NPC t = target;
			Sleep.sleepUntil(() -> inCombat() || !t.exists(), 2500);
			return true;
		}
		return false;
	}

	/**
	 * Fires the special attack if energy is at/above the threshold.
	 * @return true if the spec was toggled on.
	 */
	public boolean useSpecialIfReady(int minPercent) {
		if (Combat.getSpecialPercentage() >= minPercent && !Combat.isSpecialEnabled()) {
			Combat.toggleSpecialAttack();
			Sleep.sleep(Calculations.random(150, 350));
			return true;
		}
		return false;
	}
}
