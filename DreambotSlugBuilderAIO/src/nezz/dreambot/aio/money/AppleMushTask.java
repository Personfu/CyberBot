package nezz.dreambot.aio.money;

import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.util.PriceTracker;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Bank;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

/**
 * Makes Apple Mush at an apple press. Banks cooking apples + buckets, walks to
 * the press, makes mush (a ~12 second uninterruptible animation per bucket),
 * then banks the result. Mush is a thin-market item that consistently sells
 * well above its listed GE price.
 *
 * Requirements: start near a bank that stocks cooking apples and buckets, with
 * an apple press reachable nearby (e.g. the Brewery / orchard areas).
 */
public class AppleMushTask extends MoneyTask {

	private static final String COOKING_APPLE = "Cooking apple";
	private static final String BUCKET = "Bucket";
	private static final String APPLE_MUSH = "Apple mush";
	private static final String APPLE_PRESS = "Apple press";

	private static final int APPLES_PER_MUSH = 4;
	private static final int BUCKETS_PER_TRIP = 5;
	private static final int APPLES_PER_TRIP = BUCKETS_PER_TRIP * APPLES_PER_MUSH;

	private final PriceTracker mushPrice = new PriceTracker(APPLE_MUSH);
	private int mushMade = 0;
	private State state = State.BANK;

	private enum State { BANK, WALK_TO_PRESS, MAKE_MUSH, RETURN_BANK }

	public AppleMushTask(Config cfg) {
	}

	@Override
	public String name() {
		return "Apple Mush";
	}

	@Override
	public int execute() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		state = resolveState();
		switch (state) {
			case BANK: return handleBank();
			case WALK_TO_PRESS: return walkToPress();
			case MAKE_MUSH: return makeMush();
			case RETURN_BANK: return returnBank();
		}
		return Calculations.random(200, 400);
	}

	private State resolveState() {
		boolean hasIngredients = Inventory.contains(COOKING_APPLE) && Inventory.contains(BUCKET);
		if (Inventory.contains(APPLE_MUSH) && !hasIngredients) {
			return State.RETURN_BANK;
		}
		if (hasIngredients) {
			GameObject press = GameObjects.closest(APPLE_PRESS);
			return (press != null && press.distance() < 6) ? State.MAKE_MUSH : State.WALK_TO_PRESS;
		}
		return State.BANK;
	}

	private int handleBank() {
		if (!Bank.isOpen()) {
			Bank.open();
			Sleep.sleepUntil(Bank::isOpen, 4000);
			return Calculations.random(200, 400);
		}
		if (Inventory.contains(APPLE_MUSH)) {
			Bank.depositAll(APPLE_MUSH);
			Sleep.sleep(Calculations.random(100, 200));
		}
		Bank.depositAllExcept(COOKING_APPLE, BUCKET);
		Sleep.sleep(Calculations.random(100, 200));

		int needBuckets = BUCKETS_PER_TRIP - Inventory.count(BUCKET);
		if (needBuckets > 0) {
			if (!Bank.contains(BUCKET)) { Logger.log("[AppleMush] No buckets in bank."); return 2000; }
			Bank.withdraw(BUCKET, needBuckets);
			Sleep.sleep(Calculations.random(100, 200));
		}
		int needApples = APPLES_PER_TRIP - Inventory.count(COOKING_APPLE);
		if (needApples > 0) {
			if (!Bank.contains(COOKING_APPLE)) { Logger.log("[AppleMush] No cooking apples in bank."); return 2000; }
			Bank.withdraw(COOKING_APPLE, needApples);
			Sleep.sleep(Calculations.random(100, 200));
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
			Logger.log("[AppleMush] Apple press not found nearby.");
		}
		return Calculations.random(300, 600);
	}

	private int makeMush() {
		Item apple = Inventory.get(COOKING_APPLE);
		GameObject press = GameObjects.closest(APPLE_PRESS);
		if (apple == null || press == null)
			return Calculations.random(100, 200);

		int before = Inventory.count(APPLE_MUSH);
		Inventory.interact(apple, "Use");
		Sleep.sleep(Calculations.random(200, 400));
		press.interact("Use");
		// The animation is long (~12s) and uninterruptible; wait for a new mush.
		Sleep.sleepUntil(() -> Inventory.count(APPLE_MUSH) > before, 16000);
		int made = Inventory.count(APPLE_MUSH) - before;
		if (made > 0) mushMade += made;
		return Calculations.random(200, 400);
	}

	private int returnBank() {
		if (!Bank.isOpen()) {
			Bank.open();
			Sleep.sleepUntil(Bank::isOpen, 8000);
		}
		return Calculations.random(200, 400);
	}

	@Override
	public int getProfit() {
		return mushPrice.valueOf(mushMade);
	}

	@Override
	public String getStatus() {
		return "Apple mush made: " + mushMade;
	}
}
