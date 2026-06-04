package nezz.dreambot.unusualmoney.methods;

import nezz.dreambot.unusualmoney.gui.ScriptVars;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.List;

/**
 * Farms fire shades via Temple Trekking Route 3, first encounter.
 * Talk to Smiddi Ryak -> Route 3 -> Kill 5 fire shades -> Teleport back -> Repeat.
 * Requires: Morytania legs 3 (for quick start), combat gear, burst runes.
 * Start near Burgh de Rott / Temple Trekking start.
 */
public class FireShadesTrekking implements MoneyMethod {

	private static final String SMIDDI = "Smiddi Ryak";
	private static final String FIRE_SHADE = "Fire shade";
	private static final String FIRE_REMAINS = "Fire remains";
	private static final String MORYTANIA_LEGS = "Morytania legs 3";

	private static final Area TREK_START = new Area(3477, 3236, 3488, 3228);

	private final ScriptVars sv;
	private int remainsCollected = 0;
	private State state = State.START_TREK;

	private enum State {
		START_TREK, FIGHT_SHADES, TELEPORT_BACK
	}

	public FireShadesTrekking(ScriptVars sv) {
		this.sv = sv;
	}

	@Override
	public String getMethodName() {
		return "Fire Shades Trekking";
	}

	@Override
	public int execute() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		state = getState();

		switch (state) {
			case START_TREK:
				return startTrek();
			case FIGHT_SHADES:
				return fightShades();
			case TELEPORT_BACK:
				return teleportBack();
		}

		return Calculations.random(200, 400);
	}

	private State getState() {
		NPC shade = NPCs.closest(FIRE_SHADE);
		if (shade != null && shade.exists()) {
			return State.FIGHT_SHADES;
		}

		if (!TREK_START.contains(Players.getLocal())) {
			NPC smiddi = NPCs.closest(SMIDDI);
			if (smiddi == null) {
				return State.TELEPORT_BACK;
			}
		}

		return State.START_TREK;
	}

	private int startTrek() {
		if (Dialogues.inDialogue()) {
			return handleDialogue();
		}

		NPC smiddi = NPCs.closest(SMIDDI);
		if (smiddi != null) {
			smiddi.interact("Talk-to");
			Sleep.sleepUntil(Dialogues::inDialogue, 3000);
		} else {
			Logger.log("Cannot find Smiddi Ryak. Teleporting back.");
			state = State.TELEPORT_BACK;
		}

		return Calculations.random(300, 600);
	}

	private int handleDialogue() {
		if (Dialogues.canContinue()) {
			Dialogues.clickContinue();
			Sleep.sleep(Calculations.random(300, 600));
			return Calculations.random(200, 400);
		}

		if (Dialogues.getOptions() != null && Dialogues.getOptions().length > 0) {
			String[] options = Dialogues.getOptions();
			for (int i = 0; i < options.length; i++) {
				if (options[i].toLowerCase().contains("escort") ||
					options[i].toLowerCase().contains("route 3") ||
					options[i].toLowerCase().contains("hard")) {
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

	private int fightShades() {
		if (Inventory.contains(FIRE_REMAINS)) {
			int count = Inventory.count(FIRE_REMAINS);
			remainsCollected = Math.max(remainsCollected, count);
		}

		NPC shade = NPCs.closest(n -> n != null && n.getName().equals(FIRE_SHADE) && !n.isInCombat());
		if (shade != null) {
			if (!Players.getLocal().isInCombat()) {
				shade.interact("Attack");
				Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 3000);
			}
			return Calculations.random(600, 1000);
		}

		shade = NPCs.closest(FIRE_SHADE);
		if (shade != null && shade.exists()) {
			return Calculations.random(600, 1000);
		}

		int remains = Inventory.count(FIRE_REMAINS);
		remainsCollected += remains;
		Logger.log("Encounter complete. Fire remains this trip: " + remains);
		state = State.TELEPORT_BACK;
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
			Logger.log("No Morytania legs found. Walking back manually.");
			Walking.walk(TREK_START.getRandomTile());
			Sleep.sleepUntil(() -> TREK_START.contains(Players.getLocal()), 15000);
		}

		return Calculations.random(400, 800);
	}

	@Override
	public int getJadesSold() {
		return 0;
	}

	@Override
	public int getProfit() {
		return remainsCollected * 6400;
	}

	@Override
	public String getStatus() {
		return "Fire remains: " + remainsCollected + " | Est. Profit: " + getProfit();
	}
}
