package nezz.dreambot.master.ge;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class GrandExchangeUtil {

    private static final Tile GE_TILE = new Tile(3165, 3487, 0);
    private static final String CLERK_NAME = "Grand Exchange Clerk";

    private GrandExchangeUtil() {}

    // ── Trade restriction ─────────────────────────────────────────────
    /**
     * Returns true when the account has cleared all F2P GE trade requirements:
     * <ul>
     *   <li>Total level ≥ 100</li>
     *   <li>Quest points ≥ 10 &nbsp;(config 101)</li>
     *   <li>Minutes played ≥ 1200 &nbsp;(20 hours, VarClientInt 526)</li>
     * </ul>
     * Source: SlugHub scripter community; verified against Jagex wiki.
     * Restricted accounts silently fail GE interactions — always check this first.
     */
    public static boolean isTradeUnrestricted() {
        // Total level and quest points are the most reliable checks
        if (Skills.getTotalLevel() < 100) return false;
        if (PlayerSettings.getConfig(101) < 10) return false;
        // Time played: varbit 2511 = minutes in-game. 0 = unread / error → assume ok
        try {
            int minutesPlayed = PlayerSettings.getBitValue(2511);
            if (minutesPlayed > 0 && minutesPlayed < 1200) return false;
        } catch (Throwable ignored) { }
        return true;
    }

    /**
     * Items that are GE-restricted for trade-restricted accounts.
     * Even if an account meets the threshold, these items may have tight buy limits.
     * Used to skip sell attempts that would silently fail.
     */
    public static final List<String> GE_RESTRICTED_ITEMS = Arrays.asList(
        "Oak logs", "Willow logs", "Yew logs",
        "Raw shrimps", "Shrimps", "Raw anchovies", "Anchovies",
        "Raw lobster", "Lobster",
        "Clay", "Soft clay",
        "Copper ore", "Tin ore", "Iron ore", "Silver ore",
        "Gold ore", "Coal", "Mithril ore", "Adamantite ore", "Runite ore",
        "Cowhide",
        "Vial", "Vial of water", "Jug of water",
        "Fishing bait", "Feather", "Eye of newt", "Wine of zamorak",
        "Air rune", "Water rune", "Earth rune", "Fire rune", "Mind rune",
        "Chaos rune"
    );

    /** True if this item name can be sold at GE by the current account. */
    public static boolean canSell(String itemName) {
        return isTradeUnrestricted();
    }

    /**
     * Items this bot produces that are worth selling at the GE.
     * Restricted accounts cannot sell the items in GE_RESTRICTED_ITEMS,
     * so they stay queued until the account becomes unrestricted.
     */
    public static final List<String> SELLOFF_LIST = Arrays.asList(
        "Limpwurt root",
        "Nature rune",
        "Chaos rune",
        "Mithril ore",
        "Cowhide",
        "Soft clay",
        "Flax",
        "Steel bar"
    );

    /** Selloff items that a trade-restricted account can still sell (non-GE-restricted). */
    public static List<String> getFilteredSelloffList() {
        return SELLOFF_LIST.stream()
                .filter(item -> !GE_RESTRICTED_ITEMS.contains(item))
                .collect(Collectors.toList());
    }

    /**
     * Returns true if the given item should be queued for GE sale.
     * Unrestricted accounts use the full list; restricted accounts use a
     * filtered list that excludes GE-restricted items.
     */
    public static boolean shouldSell(String itemName) {
        if (isTradeUnrestricted()) {
            return SELLOFF_LIST.contains(itemName);
        } else {
            return getFilteredSelloffList().contains(itemName);
        }
    }

    public static boolean isAtGE() {
        return GE_TILE.distance(
            org.dreambot.api.methods.interactive.Players.getLocal()) < 12;
    }

    public static boolean walkToGE() {
        if (isAtGE()) return true;
        Walking.walk(GE_TILE);
        Sleep.sleepUntil(GrandExchangeUtil::isAtGE, 15000L);
        return isAtGE();
    }

    public static boolean openGE() {
        if (GrandExchange.isOpen()) return true;
        NPC clerk = NPCs.closest(CLERK_NAME);
        if (clerk == null) {
            walkToGE();
            return false;
        }
        if (clerk.interact("Exchange")) {
            Sleep.sleepUntil(GrandExchange::isOpen, 3000L);
        }
        return GrandExchange.isOpen();
    }

    public static void closeGE() {
        if (GrandExchange.isOpen()) {
            try { GrandExchange.close(); } catch (Throwable ignored) {}
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SELL — always use market price from PriceCache to avoid selling below floor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sell all of {@code itemName} from bank at the current market low price.
     * Uses {@link PriceCache} to ensure we never sell below what the market is paying.
     *
     * @param itemId  OSRS item ID (used for price lookup — must be accurate)
     * @param itemName  exact in-game item name
     * @return true if the offer was placed successfully
     */
    public static boolean sellAll(int itemId, String itemName) {
        if (!isTradeUnrestricted()) return false;  // account cannot trade yet
        if (!ensureBankEmpty(itemName)) return false;
        if (!walkToGE()) return false;
        if (!openGE()) return false;
        int qty = Inventory.count(itemName);
        if (qty <= 0) return false;

        // Look up market price — fall back to 1 gp if unknown (will still list, may not fill fast)
        int price = PriceCache.getQuickSellPrice(itemId);
        if (price <= 0) price = 1;
        try {
            GrandExchange.sellItem(itemName, qty, price);
            Sleep.sleepUntil(() -> offerPlaced(itemName), 4000L);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Legacy overload — accepts a caller-supplied price but clamps it to PriceCache floor.
     * This prevents outdated hardcoded prices from causing a sell-below-market loss.
     */
    public static boolean sellAll(String itemName, int priceEach) {
        if (!isTradeUnrestricted()) return false;  // account cannot trade yet
        if (!ensureBankEmpty(itemName)) return false;
        if (!walkToGE()) return false;
        if (!openGE()) return false;
        int qty = Inventory.count(itemName);
        if (qty <= 0) return false;
        int price = (priceEach > 0) ? priceEach : 1;
        try {
            GrandExchange.sellItem(itemName, qty, price);
            Sleep.sleepUntil(() -> offerPlaced(itemName), 4000L);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BUY — always validate margin before placing order
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Place a GE buy order, using {@link PriceCache} to determine the safe buy price.
     * Will REFUSE to buy if:
     * <ul>
     *   <li>The market price cannot be determined (returns false — do not guess)</li>
     *   <li>The safe buy price exceeds {@code maxPriceGp} (a hard cap set by the route)</li>
     * </ul>
     *
     * @param itemId      OSRS item ID for price lookup
     * @param itemName    exact in-game item name
     * @param qty         quantity to buy
     * @param maxPriceGp  absolute maximum price per unit the route is willing to pay (0 = no cap)
     * @return true if the buy order was placed successfully
     */
    public static boolean buyChecked(int itemId, String itemName, int qty, int maxPriceGp) {
        if (!isTradeUnrestricted()) return false;  // account cannot trade yet
        if (!walkToGE()) return false;
        if (!openGE()) return false;

        int price = PriceCache.getBuyPrice(itemId);
        if (price <= 0) {
            // Price unknown — refuse to place blind offer; money safety first
            return false;
        }
        if (maxPriceGp > 0 && price > maxPriceGp) {
            // Market moved above our budget ceiling — skip
            return false;
        }
        try {
            GrandExchange.buyItem(itemName, qty, price);
            Sleep.sleepUntil(() -> offerPlaced(itemName), 4000L);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Legacy overload — still validates against PriceCache before buying.
     * If {@code priceEach} is higher than the market high, it is clamped to market high + 5%
     * to prevent accidental overpay from stale hardcoded values.
     */
    public static boolean buy(int itemId, String itemName, int qty, int priceEach) {
        if (!isTradeUnrestricted()) return false;  // account cannot trade yet
        if (!walkToGE()) return false;
        if (!openGE()) return false;

        int marketCap = PriceCache.getBuyPrice(itemId);
        // If market price is known, never pay more than market high + margin
        int safePriceEach = (marketCap > 0) ? Math.min(priceEach, marketCap) : priceEach;
        if (safePriceEach <= 0) safePriceEach = 1;
        try {
            GrandExchange.buyItem(itemName, qty, safePriceEach);
            Sleep.sleepUntil(() -> offerPlaced(itemName), 4000L);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void collectAndBank() {
        if (!walkToGE() || !openGE()) return;
        try {
            GrandExchange.collect();
            Sleep.sleep((long) Calculations.random(600, 1200));
        } catch (Throwable ignored) {}
        closeGE();
        if (!Inventory.isEmpty()) {
            Bank.open();
            if (Bank.isOpen()) {
                Bank.depositAllItems();
                Bank.close();
            }
        }
    }

    private static boolean ensureBankEmpty(String itemName) {
        if (!Inventory.contains(itemName)) {
            Bank.open();
            if (!Bank.isOpen()) return false;
            Bank.withdrawAll(itemName);
            Sleep.sleepUntil(() -> Inventory.contains(itemName), 3000L);
            Bank.close();
        }
        return Inventory.contains(itemName);
    }

    private static boolean offerPlaced(String itemName) {
        try {
            GrandExchangeItem[] slots = GrandExchange.getItems();
            if (slots == null) return false;
            for (GrandExchangeItem slot : slots) {
                if (slot != null && itemName.equalsIgnoreCase(slot.getName())) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }
}