package nezz.dreambot.unusualmoney.methods;

import nezz.dreambot.unusualmoney.gui.ScriptVars;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;

import java.util.List;

/**
 * Buys Soul Runes from Magic Guild Store (Yanille) and Baba Yaga (Lunar Isle).
 * Hops worlds after buying out stock of 250.
 * Start at the Magic Guild store in Yanille with coins.
 */
public class SoulRuneBuyer implements MoneyMethod {

	private static final String SOUL_RUNE = "Soul rune";
	private static final String COINS = "Coins";
	private static final int STOCK_SIZE = 250;

	private static final String[] SHOP_NPCS = {"Wizard Akutha", "Baba Yaga"};

	private final ScriptVars sv;
	private int runesBought = 0;
	private boolean needsHop = false;

	public SoulRuneBuyer(ScriptVars sv) {
		this.sv = sv;
	}

	@Override
	public String getMethodName() {
		return "Soul Rune Buyer";
	}

	@Override
	public int execute() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		if (!Inventory.contains(COINS)) {
			Logger.log("Out of coins. Stopping.");
			return -1;
		}

		if (needsHop) {
			return hopWorld();
		}

		return buyRunes();
	}

	private int buyRunes() {
		if (!Shop.isOpen()) {
			NPC shopKeeper = NPCs.closest(SHOP_NPCS);
			if (shopKeeper != null) {
				shopKeeper.interact("Trade");
				Sleep.sleepUntil(Shop::isOpen, Calculations.random(1500, 2500));
			} else {
				Logger.log("Cannot find rune shop NPC. Stand near Magic Guild or Baba Yaga.");
			}
			return Calculations.random(300, 500);
		}

		Item soulRune = Shop.get(SOUL_RUNE);
		if (soulRune != null && soulRune.getAmount() > sv.soulRunesMinStock) {
			int buyAmt = soulRune.getAmount() - sv.soulRunesMinStock;
			buyAmt = (buyAmt / 10) * 10;
			if (buyAmt <= 0) buyAmt = 10;

			int before = Inventory.count(SOUL_RUNE);
			Shop.purchase(soulRune, buyAmt);
			Sleep.sleep(Calculations.random(100, 200));
			int after = Inventory.count(SOUL_RUNE);
			runesBought += (after - before);
		}

		soulRune = Shop.get(SOUL_RUNE);
		if (soulRune == null || soulRune.getAmount() <= sv.soulRunesMinStock) {
			Shop.close();
			Sleep.sleepUntil(() -> !Shop.isOpen(), 1200);
			needsHop = true;
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
		Sleep.sleepUntil(() -> Client.getInstance().getScriptManager().getCurrentScript().getRandomManager().isSolving(), 30000);
		needsHop = false;
		return Calculations.random(300, 600);
	}

	@Override
	public int getJadesSold() {
		return 0;
	}

	@Override
	public int getProfit() {
		return runesBought * 80;
	}

	@Override
	public String getStatus() {
		return "Soul runes bought: " + runesBought + " | Est. Profit: " + getProfit();
	}
}
