package nezz.dreambot.aio.boss.mechanic;

import nezz.dreambot.aio.boss.BossConfig;
import nezz.dreambot.aio.combat.CombatManager;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

/**
 * King Black Dragon's main threat is dragonfire (up to ~65 unprotected).
 * Antifire potions last ~6 minutes; we re-sip a little early so coverage never
 * lapses. An anti-dragon shield / dragonfire shield in the gear loadout plus
 * Protect from Magic (set by the boss config) covers the rest. If no antifire
 * is carried this is a no-op and we rely on the shield + prayer.
 */
public class KBDMechanic extends BossMechanic {

	private static final long ANTIFIRE_REFRESH_MS = 5 * 60 * 1000; // re-sip ~1 min early
	private long lastSip = 0;

	public KBDMechanic(BossConfig boss, CombatManager combat) {
		super(boss, combat);
	}

	@Override
	public int handle() {
		long now = System.currentTimeMillis();
		if (now - lastSip < ANTIFIRE_REFRESH_MS) {
			return NOT_HANDLED;
		}
		Item antifire = Inventory.get(i -> i != null && i.getName() != null
				&& i.getName().toLowerCase().contains("antifire"));
		if (antifire == null) {
			// No antifire carried; nothing to do here (shield + prayer cover it).
			lastSip = now;
			return NOT_HANDLED;
		}
		if (antifire.interact("Drink")) {
			Logger.log("[KBD] Refreshing antifire protection.");
			lastSip = now;
			Sleep.sleep(Calculations.random(300, 600));
			return Calculations.random(300, 500);
		}
		return NOT_HANDLED;
	}
}
