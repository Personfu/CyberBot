package nezz.dreambot.unusualmoney.methods;

import nezz.dreambot.unusualmoney.gui.ScriptVars;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;

import java.util.List;

/**
 * Sells Jades to Jiminua's/Gabooty's shop in Tai Bwo Wannai for Trading Sticks.
 * Requires: Karamja gloves equipped for bonus sticks, Jades in inventory.
 * Start near Gabooty in Tai Bwo Wannai with Jades in inventory.
 */
public class JadeTradingSticks implements MoneyMethod {

	private static final String JADE = "Jade";
	private static final String TRADING_STICKS = "Trading sticks";
	private static final String SHOP_NPC = "Gabooty";

	private final ScriptVars sv;
	private int jadesSold = 0;
	private int sticksEarned = 0;
	private boolean needsHop = false;
	private int soldThisWorld = 0;

	public JadeTradingSticks(ScriptVars sv) {
		this.sv = sv;
	}

	@Override
	public String getMethodName() {
		return "Jade Trading Sticks";
	}

	@Override
	public int execute() {
		if (!Inventory.contains(JADE)) {
			Logger.log("No jades in inventory. Bank or restock needed.");
			return Calculations.random(2000, 3000);
		}

		if (needsHop) {
			return hopWorld();
		}

		return sellJades();
	}

	private int sellJades() {
		if (!Shop.isOpen()) {
			NPC gabooty = NPCs.closest(SHOP_NPC);
			if (gabooty != null) {
				gabooty.interact("Trade");
				Sleep.sleepUntil(Shop::isOpen, Calculations.random(1500, 2500));
			} else {
				Logger.log("Cannot find Gabooty. Make sure you're in Tai Bwo Wannai.");
			}
			return Calculations.random(300, 500);
		}

		Item jade = Inventory.get(JADE);
		if (jade != null) {
			jade.interact("Sell 1");
			Sleep.sleep(Calculations.random(80, 150));
			soldThisWorld++;
			jadesSold++;

			int sticksBefore = Inventory.count(TRADING_STICKS);
			Sleep.sleep(Calculations.random(50, 100));
			int sticksAfter = Inventory.count(TRADING_STICKS);
			sticksEarned += (sticksAfter - sticksBefore);
		}

		if (soldThisWorld >= sv.jadesPerWorld) {
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
		for (int i = 0; i < 10; i++) {
			if (hopTo != Worlds.getCurrentWorld())
				break;
			hopTo = Worlds.getRandomWorld(worlds).getWorld();
		}

		Logger.log("Hopping to world: " + hopTo);
		WorldHopper.quickHop(hopTo);
		Sleep.sleepUntil(() -> org.dreambot.api.Client.getInstance().getScriptManager().getCurrentScript().getRandomManager().isSolving(), 30000);
		needsHop = false;
		return Calculations.random(300, 600);
	}

	@Override
	public int getJadesSold() {
		return jadesSold;
	}

	@Override
	public int getProfit() {
		return sticksEarned * 10;
	}

	@Override
	public String getStatus() {
		return "Jades sold: " + jadesSold + " | Sticks: " + sticksEarned + " | Est. Profit: " + getProfit();
	}
}
