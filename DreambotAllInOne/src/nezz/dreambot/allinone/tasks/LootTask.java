package nezz.dreambot.allinone.tasks;

import nezz.dreambot.allinone.config.LootEntry;
import nezz.dreambot.allinone.config.ScriptConfig;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.filter.Filter;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.GroundItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToLongFunction;

/**
 * Picks up ground items that match the active loot configuration.
 * Tracks total GP looted for the paint overlay.
 */
public class LootTask {

    private final AbstractScript script;
    private final ScriptConfig   config;

    private long totalGp = 0;

    /** Approximate GE values for items in the drop tables (GP each). Updated May 2026. */
    private static final Map<String, Long> APPROX_PRICES = new HashMap<>();
    static {
        APPROX_PRICES.put("Coins",               1L);
        APPROX_PRICES.put("Big bones",           400L);
        APPROX_PRICES.put("Dragon bones",       3_200L);
        APPROX_PRICES.put("Feathers",               2L);
        APPROX_PRICES.put("Pure essence",           5L);
        APPROX_PRICES.put("Mithril ore",          250L);
        APPROX_PRICES.put("Mossy key",          1_000L);
        APPROX_PRICES.put("Rune dagger",        4_500L);
        APPROX_PRICES.put("Granite maul",     425_000L);
        APPROX_PRICES.put("Abyssal whip",   2_200_000L);
        APPROX_PRICES.put("Abyssal dagger", 2_700_000L);
        APPROX_PRICES.put("Berserker ring",   3_000_000L);
        APPROX_PRICES.put("Archers ring",     2_900_000L);
        APPROX_PRICES.put("Seers ring",       1_800_000L);
        APPROX_PRICES.put("Dragon axe",       1_200_000L);
        APPROX_PRICES.put("Bandos chestplate", 15_000_000L);
        APPROX_PRICES.put("Bandos tassets",   25_000_000L);
        APPROX_PRICES.put("Bandos boots",      4_500_000L);
        APPROX_PRICES.put("Primordial crystal", 22_000_000L);
        APPROX_PRICES.put("Pegasian crystal",  20_000_000L);
        APPROX_PRICES.put("Eternal crystal",   15_000_000L);
        APPROX_PRICES.put("Draconic visage",    5_500_000L);
        APPROX_PRICES.put("Tanzanite fang",     4_200_000L);
        APPROX_PRICES.put("Magic fang",         3_900_000L);
        APPROX_PRICES.put("Serpentine visage",  4_800_000L);
        APPROX_PRICES.put("Dragonbone necklace",1_800_000L);
        APPROX_PRICES.put("Zulrah's scales",        175L);
        APPROX_PRICES.put("Death rune",             400L);
        APPROX_PRICES.put("Blood rune",             300L);
        APPROX_PRICES.put("Chaos rune",              90L);
        APPROX_PRICES.put("Fire rune",               10L);
        APPROX_PRICES.put("Limpwurt root",        3_000L);
        APPROX_PRICES.put("Grimy ranarr weed",   7_000L);
        APPROX_PRICES.put("Grimy snapdragon",    7_500L);
        APPROX_PRICES.put("Grimy torstol",       8_000L);
        APPROX_PRICES.put("Rune full helm",      20_000L);
        APPROX_PRICES.put("Rune 2h sword",       37_000L);
        APPROX_PRICES.put("Rune chainbody",      29_000L);
        APPROX_PRICES.put("Rune med helm",       11_000L);
        APPROX_PRICES.put("Rune crossbow",       9_000L);
        APPROX_PRICES.put("Giant key",           10_000L);
        APPROX_PRICES.put("Ensouled abyssal head", 2_000L);
        APPROX_PRICES.put("Clue scroll (medium)", 0L);
        APPROX_PRICES.put("Clue scroll (hard)",   0L);
        APPROX_PRICES.put("Clue scroll (elite)",  0L);
        APPROX_PRICES.put("KBD heads",           1_500L);
    }

    public LootTask(AbstractScript script, ScriptConfig config) {
        this.script = script;
        this.config = config;
    }

    public boolean shouldExecute() {
        if (!config.isLootEnabled()) return false;
        if (Players.getLocal() == null) return false;
        // Only loot when not in active combat
        if (Players.getLocal().isInCombat()) return false;
        return GroundItems.closest(lootFilter()) != null;
    }

    public int execute() {
        GroundItem gi = GroundItems.closest(lootFilter());
        if (gi == null) return Calculations.random(300, 600);

        if (!gi.isOnScreen()) {
            Walking.walk(gi.getTile());
            Sleep.sleepUntil(() -> gi.isOnScreen(), 2000);
            return Calculations.random(300, 600);
        }

        int qtyBefore = gi.getAmount();
        gi.interact("Take");
        Sleep.sleepUntil(() -> !gi.exists(), 1500);

        // Track GP
        long price = APPROX_PRICES.getOrDefault(gi.getName(), 0L);
        totalGp += price * qtyBefore;

        return Calculations.random(300, 600);
    }

    private Filter<GroundItem> lootFilter() {
        return gi -> {
            if (gi == null || !gi.exists() || gi.getName() == null) return false;

            // Distance check
            if (Players.getLocal() != null) {
                double dist = Players.getLocal().getTile().distance(gi.getTile());
                if (dist > config.getLootRadiusTiles()) return false;
            }

            // Match against enabled loot entries
            for (LootEntry entry : config.getLootEntries()) {
                if (!entry.isEnabled()) continue;
                if (!gi.getName().equalsIgnoreCase(entry.getItemName())) continue;

                // Quantity filter
                if (gi.getAmount() < entry.getMinPickupQty()) return false;

                // GP value filter
                if (entry.getMinPickupGeValue() > 0) {
                    long price = APPROX_PRICES.getOrDefault(gi.getName(), 0L);
                    if (price * gi.getAmount() < entry.getMinPickupGeValue()) return false;
                }
                return true;
            }
            return false;
        };
    }

    public long getTotalGp() { return totalGp; }

    /** Returns the approximate GE price for an item name. */
    public static long approxPrice(String itemName) {
        return APPROX_PRICES.getOrDefault(itemName, 0L);
    }

    /**
     * Static ToLongFunction reference usable by the GUI's value filter
     * (the GUI does not hold an instance of LootTask at build time).
     */
    public static final ToLongFunction<String> approxPriceStatic =
        name -> APPROX_PRICES.getOrDefault(name, 0L);
}
