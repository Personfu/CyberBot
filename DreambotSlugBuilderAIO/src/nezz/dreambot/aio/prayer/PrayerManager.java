package nezz.dreambot.aio.prayer;

import org.dreambot.api.methods.prayer.Prayer;
import org.dreambot.api.methods.prayer.Prayers;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

/**
 * Centralises overhead and offensive prayer handling. Keeps exactly one
 * protection prayer active and (optionally) one offensive prayer, switching
 * efficiently and only issuing toggles when the desired state differs from the
 * current one (so it doubles as a flicker when called every tick).
 */
public class PrayerManager {

	public enum Protect { NONE, MELEE, MAGIC, RANGED }

	/**
	 * Ensures the requested overhead protection is the only one active.
	 */
	public void setProtection(Protect protect) {
		Prayer wanted = toPrayer(protect);
		for (Protect p : Protect.values()) {
			Prayer pr = toPrayer(p);
			if (pr == null) continue;
			boolean shouldBeOn = pr.equals(wanted);
			if (shouldBeOn && !Prayers.isActive(pr) && hasLevel(pr)) {
				Prayers.toggle(true, pr);
			} else if (!shouldBeOn && Prayers.isActive(pr)) {
				Prayers.toggle(false, pr);
			}
		}
	}

	private Prayer toPrayer(Protect protect) {
		switch (protect) {
			case MELEE: return Prayer.PROTECT_FROM_MELEE;
			case MAGIC: return Prayer.PROTECT_FROM_MAGIC;
			case RANGED: return Prayer.PROTECT_FROM_MISSILES;
			default: return null;
		}
	}

	/** Enables the best available offensive prayer for the given style if not active. */
	public void enableOffensive(Skill style) {
		Prayer pr = bestOffensive(style);
		if (pr != null && hasLevel(pr) && !Prayers.isActive(pr)) {
			Prayers.toggle(true, pr);
		}
	}

	private Prayer bestOffensive(Skill style) {
		switch (style) {
			case RANGED:
				return has(Prayer.RIGOUR) ? Prayer.RIGOUR : Prayer.EAGLE_EYE;
			case MAGIC:
				return has(Prayer.AUGURY) ? Prayer.AUGURY : Prayer.MYSTIC_MIGHT;
			default:
				return has(Prayer.PIETY) ? Prayer.PIETY : Prayer.ULTIMATE_STRENGTH;
		}
	}

	private boolean has(Prayer p) {
		return hasLevel(p);
	}

	/** Required prayer levels for the prayers we use (avoids API coupling). */
	private boolean hasLevel(Prayer p) {
		int lvl = Skills.getRealLevel(Skill.PRAYER);
		if (p == Prayer.AUGURY) return lvl >= 77;
		if (p == Prayer.RIGOUR) return lvl >= 74;
		if (p == Prayer.PIETY) return lvl >= 70;
		if (p == Prayer.MYSTIC_MIGHT) return lvl >= 45;
		if (p == Prayer.EAGLE_EYE) return lvl >= 44;
		if (p == Prayer.PROTECT_FROM_MAGIC) return lvl >= 37;
		if (p == Prayer.PROTECT_FROM_MISSILES) return lvl >= 40;
		if (p == Prayer.PROTECT_FROM_MELEE) return lvl >= 43;
		if (p == Prayer.ULTIMATE_STRENGTH) return lvl >= 31;
		return true;
	}

	public int points() {
		return Prayers.getPoints();
	}

	public void disableAll() {
		for (Protect p : Protect.values()) {
			Prayer pr = toPrayer(p);
			if (pr != null && Prayers.isActive(pr)) {
				Prayers.toggle(false, pr);
			}
		}
	}
}
