package nezz.dreambot.aio.money;

import nezz.dreambot.aio.gui.Config;
import nezz.dreambot.aio.util.PriceTracker;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;

import java.util.List;

/**
 * Sells Jades to Gabooty in Tai Bwo Wannai for Trading Sticks, then hops worlds
 * once a configurable number have been sold. Trading sticks are worth ~10 GP
 * each on the GE, while the jade only costs ~290, so the effective value of a
 * jade is roughly 10x its GE price.
 *
 * Requirements: stand near Gabooty with jades in inventory, Karamja gloves
 * equipped for the 120-stick payout tier.
 */
public class JadeTradingSticksTask extends MoneyTask {

	private static final String JADE = "Jade";
	private static final String TRADING_STICKS = "Trading sticks";
	private static final String SHOP_NPC = "Gabooty";

	private final Config cfg;
	private final PriceTracker stickPrice = new PriceTracker(TRADING_STICKS);

	private int jadesSold = 0;
	private int sticksEarned = 0;
	private int soldThisWorld = 0;
	private boolean needsHop = false;

	public JadeTradingSticksTask(Config cfg) {
		this.cfg = cfg;
	}

	@Override
	public String name() {
		return "Jade -> Trading Sticks";
	}

	@Override
	public int execute() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		if (!Inventory.contains(JADE) && !needsHop) {
			Logger.log("[Jade] Out of jades. Restock required.");
			return Calculations.random(2000, 3000);
		}

		if (needsHop)
			return hopWorld();

		return sellJades();
	}

	private int sellJades() {
		if (!Shop.isOpen()) {
			NPC gabooty = NPCs.closest(SHOP_NPC);
			if (gabooty != null) {
				gabooty.interact("Trade");
				Sleep.sleepUntil(Shop::isOpen, Calculations.random(1500, 2500));
			} else {
				Logger.log("[Jade] Gabooty not found - are you in Tai Bwo Wannai?");
			}
			return Calculations.random(300, 500);
		}

		Item jade = Inventory.get(JADE);
		if (jade != null) {
			int before = Inventory.count(TRADING_STICKS);
			jade.interact("Sell 1");
			Sleep.sleepUntil(() -> Inventory.count(TRADING_STICKS) > before, 1200);
			int gained = Inventory.count(TRADING_STICKS) - before;
			if (gained > 0) {
				sticksEarned += gained;
				jadesSold++;
				soldThisWorld++;
			}
		}

		if (soldThisWorld >= cfg.jadesPerWorld || !Inventory.contains(JADE)) {
			Shop.close();
			Sleep.sleepUntil(() -> !Shop.isOpen(), 1200);
			needsHop = true;
			soldThisWorld = 0;
		}

		return Calculations.random(80, 150);
	}

	private int hopWorld() {
		if (Shop.isOpen()) {
			Shop.close();
			Sleep.sleepUntil(() -> !Shop.isOpen(), 1200);
		}
		List<World> worlds = Worlds.all(w -> !w.isF2P() && !w.isPVP() && !w.isHighRisk());
		int hopTo = Worlds.getRandomWorld(worlds).getWorld();
		for (int i = 0; i < 10 && hopTo == Worlds.getCurrentWorld(); i++) {
			hopTo = Worlds.getRandomWorld(worlds).getWorld();
		}
		Logger.log("[Jade] Hopping to world " + hopTo);
		WorldHopper.quickHop(hopTo);
		Sleep.sleep(Calculations.random(2000, 4000));
		needsHop = false;
		return Calculations.random(300, 600);
	}

	@Override
	public int getProfit() {
		return stickPrice.valueOf(sticksEarned);
	}

	@Override
	public String getStatus() {
		return "Jades sold: " + jadesSold + " | Sticks: " + sticksEarned;
	}
}
