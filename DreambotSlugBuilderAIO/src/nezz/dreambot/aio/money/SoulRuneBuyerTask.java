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
 * Buys Soul Runes from the Magic Guild store (Yanille) or Baba Yaga's Magic
 * Shop (Lunar Isle) and hops worlds once the stock is depleted. The shop price
 * (~300) sits well under the GE price (~410), making it a classic hop-and-shop.
 *
 * Requirements: stand next to the rune shopkeeper with coins. Stops cleanly
 * when coins run out.
 */
public class SoulRuneBuyerTask extends MoneyTask {

	private static final String SOUL_RUNE = "Soul rune";
	private static final String COINS = "Coins";
	private static final String[] SHOP_NPCS = {"Wizard", "Baba Yaga"};

	private final Config cfg;
	private final PriceTracker runePrice = new PriceTracker(SOUL_RUNE);

	private int runesBought = 0;
	private int coinsSpent = 0;
	private boolean needsHop = false;

	public SoulRuneBuyerTask(Config cfg) {
		this.cfg = cfg;
	}

	@Override
	public String name() {
		return "Soul Rune Buyer";
	}

	@Override
	public int execute() {
		if (!Client.isLoggedIn())
			return Calculations.random(300, 500);

		if (!Inventory.contains(COINS)) {
			Logger.log("[SoulRune] Out of coins - stopping.");
			return -1;
		}

		if (needsHop)
			return hopWorld();

		return buyRunes();
	}

	private int buyRunes() {
		if (!Shop.isOpen()) {
			NPC keeper = NPCs.closest(n -> {
				if (n == null || n.getName() == null) return false;
				for (String s : SHOP_NPCS) {
					if (n.getName().contains(s)) return true;
				}
				return false;
			});
			if (keeper != null) {
				keeper.interact("Trade");
				Sleep.sleepUntil(Shop::isOpen, Calculations.random(1500, 2500));
			} else {
				Logger.log("[SoulRune] Shopkeeper not found - stand near Magic Guild or Baba Yaga.");
			}
			return Calculations.random(300, 500);
		}

		Item rune = Shop.get(SOUL_RUNE);
		if (rune != null && rune.getAmount() > cfg.soulRunesMinStock) {
			int buyAmt = rune.getAmount() - cfg.soulRunesMinStock;
			buyAmt = (buyAmt / 10) * 10;
			if (buyAmt <= 0) buyAmt = 10;

			int beforeRunes = Inventory.count(SOUL_RUNE);
			int beforeCoins = Inventory.count(COINS);
			Shop.purchase(rune, buyAmt);
			Sleep.sleep(Calculations.random(120, 220));
			runesBought += Math.max(0, Inventory.count(SOUL_RUNE) - beforeRunes);
			coinsSpent += Math.max(0, beforeCoins - Inventory.count(COINS));
		}

		rune = Shop.get(SOUL_RUNE);
		if (rune == null || rune.getAmount() <= cfg.soulRunesMinStock) {
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
		for (int i = 0; i < 10 && hopTo == Worlds.getCurrentWorld(); i++) {
			hopTo = Worlds.getRandomWorld(worlds).getWorld();
		}
		Logger.log("[SoulRune] Hopping to world " + hopTo);
		WorldHopper.quickHop(hopTo);
		Sleep.sleep(Calculations.random(2000, 4000));
		needsHop = false;
		return Calculations.random(300, 600);
	}

	@Override
	public int getProfit() {
		return runePrice.valueOf(runesBought) - coinsSpent;
	}

	@Override
	public String getStatus() {
		return "Runes: " + runesBought + " | Spent: " + coinsSpent;
	}
}
