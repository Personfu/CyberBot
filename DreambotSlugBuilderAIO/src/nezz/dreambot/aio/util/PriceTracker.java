package nezz.dreambot.aio.util;

import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.utilities.Logger;

/**
 * Caches a live Grand Exchange price for an item and lets callers compute the
 * value of an arbitrary quantity. Price lookups are cached for a short window
 * to avoid hammering the price service.
 */
public class PriceTracker {

	private final String itemName;
	private int price;
	private long lastFetch = 0;
	private static final long CACHE_MS = 60_000;

	public PriceTracker(String itemName) {
		this.itemName = itemName;
		refresh();
	}

	public void refresh() {
		try {
			int p = LivePrices.get(itemName);
			if (p > 0) {
				price = p;
			}
			lastFetch = System.currentTimeMillis();
		} catch (Throwable t) {
			Logger.log("[PriceTracker] Failed to fetch price for " + itemName + ": " + t.getMessage());
		}
	}

	public int getPrice() {
		if (System.currentTimeMillis() - lastFetch > CACHE_MS) {
			refresh();
		}
		return price;
	}

	public int valueOf(int amount) {
		return Math.max(0, amount) * getPrice();
	}

	public String getItemName() {
		return itemName;
	}
}
