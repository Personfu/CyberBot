package nezz.dreambot.master.ge;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Real-time item price lookup via the OSRS Wiki Prices API.
 *
 * <h3>Why this exists</h3>
 * Hardcoded GP values in money routes go stale within days and cause the bot
 * to either:
 * <ul>
 *   <li>Buy above market → immediate loss before the item is even processed</li>
 *   <li>Sell below market → leaving gold on the table every batch</li>
 *   <li>Place offers that never fill → gold locked in the GE indefinitely</li>
 * </ul>
 *
 * <h3>API</h3>
 * Uses {@code https://prices.runescape.wiki/api/v1/osrs/latest?id=X}.
 * Returns {@code {"data":{"ID":{"high":NNN,"low":NNN,...}}}}.
 * "high" = what buyers are paying (insta-buy). "low" = what sellers receive (insta-sell).
 *
 * <h3>Buy / sell margins</h3>
 * <ul>
 *   <li>Buy at market high + {@value #BUY_MARGIN_PCT}% → offer fills fast, no overpay risk</li>
 *   <li>Sell at market low - {@value #SELL_MARGIN_PCT}% → offer fills fast, never below floor</li>
 * </ul>
 * All prices are cached for {@value #CACHE_TTL_MIN} minutes per item ID.
 *
 * <h3>Failure handling</h3>
 * If the API is unreachable or returns bad data, stale cache is used if available.
 * If no cache exists, methods return 0 — callers must treat 0 as "price unknown, skip
 * this trade" to avoid placing offers at nonsense values.
 */
public final class PriceCache {

    // ── API ───────────────────────────────────────────────────────────────────
    private static final String BASE_URL   = "https://prices.runescape.wiki/api/v1/osrs/latest?id=";
    private static final String USER_AGENT = "CyberBot/2.0 (github.com/Personfu/CyberBot; F2P OSRS learning project)";
    private static final int    TIMEOUT_MS = 4_000;

    // ── Cache ─────────────────────────────────────────────────────────────────
    private static final int  CACHE_TTL_MIN = 10;
    private static final long CACHE_TTL_MS  = CACHE_TTL_MIN * 60_000L;
    /** long[0]=high, long[1]=low, long[2]=fetchedAt (epoch ms) */
    private static final Map<Integer, long[]> CACHE = new HashMap<>();

    // ── Trade margins ─────────────────────────────────────────────────────────
    /** Pay market high + this % to guarantee buy fills. */
    public static final int BUY_MARGIN_PCT  = 5;
    /** Sell at market low - this % to guarantee sell fills. */
    public static final int SELL_MARGIN_PCT = 3;
    /** Minimum viable profit margin per batch (GP). Routes skip the trade if below this. */
    public static final int MIN_PROFIT_GP   = 5_000;

    private PriceCache() {}

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Safe buy price to use in a GE buy offer: market high + {@value #BUY_MARGIN_PCT}%.
     * @return price in GP, or 0 if unknown (caller must not place offer at 0)
     */
    public static int getBuyPrice(int itemId) {
        long[] d = lookup(itemId);
        if (d == null || d[0] <= 0) return 0;
        return (int)(d[0] * (1.0 + BUY_MARGIN_PCT / 100.0));
    }

    /**
     * Safe sell price to use in a GE sell offer: market low - {@value #SELL_MARGIN_PCT}%.
     * @return price in GP, or 0 if unknown
     */
    public static int getSellPrice(int itemId) {
        long[] d = lookup(itemId);
        if (d == null || d[1] <= 0) return 0;
        return Math.max(1, (int)(d[1] * (1.0 - SELL_MARGIN_PCT / 100.0)));
    }

    /** Raw insta-buy price (what buyers pay). 0 if unknown. */
    public static int getHighPrice(int itemId) {
        long[] d = lookup(itemId);
        return (d != null && d[0] > 0) ? (int) d[0] : 0;
    }

    /** Raw insta-sell price (what sellers receive). 0 if unknown. */
    public static int getLowPrice(int itemId) {
        long[] d = lookup(itemId);
        return (d != null && d[1] > 0) ? (int) d[1] : 0;
    }

    /**
     * Returns true only if the arithmetic is solid:
     * {@code (sellPrice × outputQty) - inputCostGp >= minProfit}.
     *
     * @param inputCostGp  total cost of all inputs (use {@link #getBuyPrice} per input × qty)
     * @param outputItemId item ID of the thing you are selling
     * @param outputQty    how many you are selling
     * @param minProfit    minimum required net profit in GP (use {@link #MIN_PROFIT_GP} as default)
     */
    public static boolean isProfitable(long inputCostGp, int outputItemId, int outputQty, int minProfit) {
        int sell = getSellPrice(outputItemId);
        if (sell <= 0) return false;
        return ((long) sell * outputQty) - inputCostGp >= minProfit;
    }

    /**
     * Best offer price for selling: use the raw low price clamped to ≥1.
     * Slightly more aggressive than {@link #getSellPrice} — for situations
     * where you just want to move stock, not maximise margin.
     */
    public static int getQuickSellPrice(int itemId) {
        long[] d = lookup(itemId);
        if (d == null || d[1] <= 0) return 1;
        return Math.max(1, (int) d[1]);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static synchronized long[] lookup(int itemId) {
        long now = System.currentTimeMillis();
        long[] cached = CACHE.get(itemId);
        if (cached != null && (now - cached[2]) < CACHE_TTL_MS) {
            return cached; // fresh
        }
        // Fetch from API
        try {
            String json = get(BASE_URL + itemId);
            if (json == null || json.isEmpty()) return cached; // stale fallback
            long high = parseNumber(json, "\"high\":");
            long low  = parseNumber(json, "\"low\":");
            if (high <= 0 && low <= 0) return cached;
            long[] fresh = { high, low, now };
            CACHE.put(itemId, fresh);
            return fresh;
        } catch (Throwable t) {
            return cached; // return stale rather than nothing
        }
    }

    private static String get(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection c = (HttpURLConnection) url.openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(TIMEOUT_MS);
        c.setReadTimeout(TIMEOUT_MS);
        c.setRequestProperty("User-Agent", USER_AGENT);
        c.setRequestProperty("Accept", "application/json");
        if (c.getResponseCode() != 200) return null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    /** Zero-dependency JSON field extractor — reads first occurrence of {@code key} value. */
    private static long parseNumber(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return 0;
        int i = idx + key.length();
        while (i < json.length() && (json.charAt(i) == ' ' || json.charAt(i) == '\t')) i++;
        int start = i;
        if (i < json.length() && json.charAt(i) == '-') i++;
        while (i < json.length() && Character.isDigit(json.charAt(i))) i++;
        if (i == start || (i == start + 1 && json.charAt(start) == '-')) return 0;
        try { return Long.parseLong(json.substring(start, i)); }
        catch (NumberFormatException e) { return 0; }
    }
}
