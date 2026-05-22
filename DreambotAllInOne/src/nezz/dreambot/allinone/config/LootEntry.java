package nezz.dreambot.allinone.config;

/**
 * One entry in an NPC's drop table.
 * Stores the OSRS drop rate alongside user-configurable pick-up thresholds.
 */
public class LootEntry {

    private final String itemName;
    private final int    itemId;
    private final int    minQty;
    private final int    maxQty;
    /** Drop-table numerator (almost always 1). */
    private final int    rateNumerator;
    /**
     * Drop-table denominator.  0 or 1 means "always drops".
     * e.g. rateNumerator=1, rateDenominator=512 → 1/512 per kill.
     * For weight-based tables this is the weightedTotal / weight,
     * rounded to the nearest integer.
     */
    private final int    rateDenominator;

    // ── User-configurable at runtime (edited in the Loot Config tab) ──
    private boolean enabled         = true;
    private int     minPickupQty    = 1;
    private long    minPickupGeValue = 0;   // 0 = no GP filter

    public LootEntry(String itemName, int itemId, int minQty, int maxQty,
                     int rateNumerator, int rateDenominator) {
        this.itemName        = itemName;
        this.itemId          = itemId;
        this.minQty          = minQty;
        this.maxQty          = maxQty;
        this.rateNumerator   = rateNumerator;
        this.rateDenominator = rateDenominator;
    }

    /** Convenience: always-drop entry (bones, ashes, etc.). */
    public static LootEntry always(String name, int id, int qty) {
        return new LootEntry(name, id, qty, qty, 1, 1);
    }

    /** Convenience: range always-drop (e.g. 5–15 feathers). */
    public static LootEntry alwaysRange(String name, int id, int minQty, int maxQty) {
        return new LootEntry(name, id, minQty, maxQty, 1, 1);
    }

    /** Convenience: weight-based drop given weight and total-weight. */
    public static LootEntry weighted(String name, int id, int minQty, int maxQty,
                                     int weight, int weightedTotal) {
        int denom = (weight <= 0) ? 1 : Math.round((float) weightedTotal / weight);
        return new LootEntry(name, id, minQty, maxQty, 1, denom);
    }

    /** Convenience: fixed fractional rate (e.g. 1/512). */
    public static LootEntry rate(String name, int id, int qty, int denominator) {
        return new LootEntry(name, id, qty, qty, 1, denominator);
    }

    /** Convenience: fixed fractional rate with quantity range. */
    public static LootEntry rateRange(String name, int id, int minQty, int maxQty, int denominator) {
        return new LootEntry(name, id, minQty, maxQty, 1, denominator);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String  getItemName()          { return itemName; }
    public int     getItemId()            { return itemId; }
    public int     getMinQty()            { return minQty; }
    public int     getMaxQty()            { return maxQty; }
    public int     getRateNumerator()     { return rateNumerator; }
    public int     getRateDenominator()   { return rateDenominator; }
    public boolean isEnabled()            { return enabled; }
    public int     getMinPickupQty()      { return minPickupQty; }
    public long    getMinPickupGeValue()  { return minPickupGeValue; }

    // ── Setters (runtime config) ──────────────────────────────────────────────

    public void setEnabled(boolean v)          { this.enabled          = v; }
    public void setMinPickupQty(int v)         { this.minPickupQty     = Math.max(1, v); }
    public void setMinPickupGeValue(long v)    { this.minPickupGeValue = Math.max(0, v); }

    // ── Display helpers ───────────────────────────────────────────────────────

    /** Formatted rate: "Always", "1/512", "12/256". */
    public String rateString() {
        if (rateDenominator <= 1) return "Always";
        return rateNumerator + "/" + rateDenominator;
    }

    /** Average drops over 1,000 simulated kills. */
    public double expectedPer1000() {
        if (rateDenominator <= 1) return 1000.0;
        return 1000.0 * rateNumerator / rateDenominator;
    }

    @Override
    public String toString() {
        return itemName + " [" + rateString() + "]";
    }
}
