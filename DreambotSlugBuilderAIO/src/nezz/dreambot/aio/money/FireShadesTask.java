package nezz.dreambot.aio.money;

import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.util.PriceTracker;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;

/**
 * Farms the first encounter of Temple Trekking Route 3 (five Fire Shades) for
 * Fire Remains. After the encounter is cleared, teleport back with Morytania
 * legs 3 and restart the trek - you never finish the trek, just the first
 * combat encounter.
 *
 * Requirements: start near Smiddi Ryak in Mortania with combat gear, burst
 * runes and Morytania legs 3 (or hard diary teleport) for fast resets.
 */
public class FireShadesTask extends MoneyTask {

	private static final String SMIDDI = "Smiddi Ryak";
	private static final String FIRE_SHADE = "Fire shade";
	private static final String FIRE_REMAINS = "Fire remains";
	private static final String MORYTANIA_LEGS = "Morytania legs 3";

	private static final Area TREK_START = new Area(3477, 3236, 3488, 3228);

	private final PriceTracker remainsPrice = new PriceTracker(FIRE_REMAINS);
	private int remainsCollected = 0;
	private int lastRemainsInInv = 0;

	public FireShadesTask(Config cfg) {
	}

	@Override
	public String name() {
		return "Fire Shades (Temple Trekking)";
	}

	@Override
	public int execute() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		trackRemains();

		NPC shade = NPCs.closest(FIRE_SHADE);
		if (shade != null && shade.exists()) {
			return fight(shade);
		}

		// No shades present: either start a fresh trek or teleport back first.
		if (TREK_START.contains(Players.getLocal()) || NPCs.closest(SMIDDI) != null) {
			return startTrek();
		}
		return teleportBack();
	}

	private void trackRemains() {
		int now = Inventory.count(FIRE_REMAINS);
		if (now > lastRemainsInInv) {
			remainsCollected += (now - lastRemainsInInv);
		}
		lastRemainsInInv = now;
	}

	private int fight(NPC nearest) {
		if (!Players.getLocal().isInCombat()) {
			NPC target = NPCs.closest(n -> n != null && FIRE_SHADE.equals(n.getName()) && !n.isInCombat());
			if (target == null) target = nearest;
			if (target != null) {
				target.interact("Attack");
				Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 3000);
			}
		}
		return Calculations.random(600, 1000);
	}

	private int startTrek() {
		if (Dialogues.inDialogue())
			return handleDialogue();

		NPC smiddi = NPCs.closest(SMIDDI);
		if (smiddi != null) {
			smiddi.interact("Talk-to");
			Sleep.sleepUntil(Dialogues::inDialogue, 3000);
		} else {
			Logger.log("[FireShades] Smiddi Ryak not found.");
		}
		return Calculations.random(300, 600);
	}

	private int handleDialogue() {
		if (Dialogues.canContinue()) {
			Dialogues.clickContinue();
			Sleep.sleep(Calculations.random(300, 600));
			return Calculations.random(200, 400);
		}
		String[] options = Dialogues.getOptions();
		if (options != null && options.length > 0) {
			for (int i = 0; i < options.length; i++) {
				String o = options[i].toLowerCase();
				if (o.contains("escort") || o.contains("route 3") || o.contains("hard")) {
					Dialogues.chooseOption(i + 1);
					Sleep.sleep(Calculations.random(400, 800));
					return Calculations.random(200, 400);
				}
			}
			Dialogues.chooseOption(1);
			Sleep.sleep(Calculations.random(400, 800));
		}
		return Calculations.random(200, 400);
	}

	private int teleportBack() {
		if (Equipment.contains(MORYTANIA_LEGS)) {
			Equipment.interact(MORYTANIA_LEGS, "Burgh de Rott");
			Sleep.sleepUntil(() -> TREK_START.contains(Players.getLocal()), 8000);
		} else if (Inventory.contains(MORYTANIA_LEGS)) {
			Inventory.interact(MORYTANIA_LEGS, "Burgh de Rott");
			Sleep.sleepUntil(() -> TREK_START.contains(Players.getLocal()), 8000);
		} else {
			Logger.log("[FireShades] No Morytania legs - walking back.");
			Walking.walk(TREK_START.getRandomTile());
			Sleep.sleepUntil(() -> TREK_START.contains(Players.getLocal()), 15000);
		}
		return Calculations.random(400, 800);
	}

	@Override
	public int getProfit() {
		return remainsPrice.valueOf(remainsCollected);
	}

	@Override
	public String getStatus() {
		return "Fire remains: " + remainsCollected;
	}
}
