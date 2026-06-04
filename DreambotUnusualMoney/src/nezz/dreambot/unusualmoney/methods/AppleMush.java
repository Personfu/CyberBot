package nezz.dreambot.unusualmoney.methods;

import nezz.dreambot.unusualmoney.gui.ScriptVars;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Bank;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

/**
 * Makes Apple Mush at the apple press in Al Kharid/Cooking Guild area.
 * Banks for cooking apples + buckets, runs to apple press, makes mush, banks.
 * Start near a bank with cooking apples and buckets available.
 */
public class AppleMush implements MoneyMethod {

	private static final String COOKING_APPLE = "Cooking apple";
	private static final String BUCKET = "Bucket";
	private static final String APPLE_MUSH = "Apple mush";
	private static final String APPLE_PRESS = "Apple press";

	private static final int APPLES_PER_MUSH = 4;
	private static final int BUCKETS_PER_TRIP = 5;
	private static final int APPLES_PER_TRIP = BUCKETS_PER_TRIP * APPLES_PER_MUSH;

	private final ScriptVars sv;
	private int mushMade = 0;
	private State state = State.BANK;

	private enum State {
		BANK, WALK_TO_PRESS, MAKE_MUSH, WALK_TO_BANK
	}

	public AppleMush(ScriptVars sv) {
		this.sv = sv;
	}

	@Override
	public String getMethodName() {
		return "Apple Mush";
	}

	@Override
	public int execute() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		state = getState();

		switch (state) {
			case BANK:
				return handleBank();
			case WALK_TO_PRESS:
				return walkToPress();
			case MAKE_MUSH:
				return makeMush();
			case WALK_TO_BANK:
				return walkToBank();
		}

		return Calculations.random(200, 400);
	}

	private State getState() {
		if (Inventory.contains(APPLE_MUSH) && !Inventory.contains(COOKING_APPLE)) {
			return State.WALK_TO_BANK;
		}
		if (Inventory.contains(COOKING_APPLE) && Inventory.contains(BUCKET)) {
			GameObject press = GameObjects.closest(APPLE_PRESS);
			if (press != null && press.distance() < 10) {
				return State.MAKE_MUSH;
			}
			return State.WALK_TO_PRESS;
		}
		return State.BANK;
	}

	private int handleBank() {
		if (!Bank.isOpen()) {
			Bank.open();
			Sleep.sleepUntil(Bank::isOpen, 2000);
			return Calculations.random(200, 400);
		}

		if (Inventory.contains(APPLE_MUSH)) {
			Bank.depositAll(APPLE_MUSH);
			Sleep.sleep(Calculations.random(100, 200));
		}

		Bank.depositAllExcept(COOKING_APPLE, BUCKET);
		Sleep.sleep(Calculations.random(100, 200));

		if (!Inventory.contains(BUCKET) || Inventory.count(BUCKET) < BUCKETS_PER_TRIP) {
			int needed = BUCKETS_PER_TRIP - Inventory.count(BUCKET);
			if (needed > 0) {
				Bank.withdraw(BUCKET, needed);
				Sleep.sleep(Calculations.random(100, 200));
			}
		}

		if (!Inventory.contains(COOKING_APPLE) || Inventory.count(COOKING_APPLE) < APPLES_PER_TRIP) {
			int needed = APPLES_PER_TRIP - Inventory.count(COOKING_APPLE);
			if (needed > 0) {
				Bank.withdraw(COOKING_APPLE, needed);
				Sleep.sleep(Calculations.random(100, 200));
			}
		}

		if (!Bank.contains(COOKING_APPLE) || !Bank.contains(BUCKET)) {
			Logger.log("Out of cooking apples or buckets in bank.");
		}

		Bank.close();
		Sleep.sleepUntil(() -> !Bank.isOpen(), 1200);
		return Calculations.random(200, 400);
	}

	private int walkToPress() {
		GameObject press = GameObjects.closest(APPLE_PRESS);
		if (press != null) {
			Walking.walk(press.getTile());
			Sleep.sleepUntil(() -> {
				GameObject p = GameObjects.closest(APPLE_PRESS);
				return p != null && p.distance() < 5;
			}, 8000);
		} else {
			Logger.log("Cannot find apple press. Walk closer to one.");
		}
		return Calculations.random(300, 600);
	}

	private int makeMush() {
		if (!Inventory.contains(COOKING_APPLE) || !Inventory.contains(BUCKET)) {
			return Calculations.random(100, 200);
		}

		Item apple = Inventory.get(COOKING_APPLE);
		GameObject press = GameObjects.closest(APPLE_PRESS);

		if (apple != null && press != null) {
			Inventory.interact(apple, "Use");
			Sleep.sleep(Calculations.random(200, 400));
			press.interact("Use");
			Sleep.sleepUntil(() -> Players.getLocal().getAnimation() != -1, 5000);
			Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == -1, 15000);
			mushMade++;
		}

		return Calculations.random(200, 400);
	}

	private int walkToBank() {
		if (Bank.isOpen())
			return Calculations.random(100, 200);

		Bank.open();
		Sleep.sleepUntil(Bank::isOpen, 8000);
		return Calculations.random(200, 400);
	}

	@Override
	public int getJadesSold() {
		return 0;
	}

	@Override
	public int getProfit() {
		return mushMade * 6900;
	}

	@Override
	public String getStatus() {
		return "Apple mush made: " + mushMade + " | Est. Profit: " + getProfit();
	}
}
