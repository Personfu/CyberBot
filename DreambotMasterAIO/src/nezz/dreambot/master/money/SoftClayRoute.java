package nezz.dreambot.master.money;

import nezz.dreambot.master.ge.GESellTask;
import nezz.dreambot.master.ge.PriceCache;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

import java.util.Collections;
import java.util.Map;

/**
 * Soft clay production — mine clay, soften with bucket of water, sell at GE.
 *
 * <h3>Why this is a top underused F2P method (May 2026)</h3>
 * <ul>
 *   <li><b>Zero requirements</b> — any fresh account can do this immediately.</li>
 *   <li><b>Almost no bot competition</b> — most bots go straight to chickens/cows;
 *       they skip the mining-adjacent methods because the "use item on item" step is extra state.</li>
 *   <li><b>Soft clay demand is always high</b> — used in Construction (P2P) and
 *       Crafting; always bought on GE by members.</li>
 *   <li><b>Fast banking</b> — Varrock East mine to bank is about 30 seconds walk.</li>
 * </ul>
 *
 * <h3>Profit (May 2026 estimate)</h3>
 * Soft clay ~250-350 gp. Mining ~200 clay/hr → <b>50,000–70,000 gp/hr</b>.
 * No input cost; pure profit.
 *
 * <h3>Route</h3>
 * Varrock East mine (clay rocks, Tile ~3286, 3370) → Varrock East bank (Tile 3253, 3420).
 * The trip: mine 14 clay, use 14 bucket-of-water → 14 soft clay, bank, repeat.
 *
 * <h3>OSRS item IDs</h3>
 * Clay = 434, Soft clay = 1761, Bucket = 1925, Bucket of water = 1929.
 */
public final class SoftClayRoute extends MoneyRoute {

    // ── Tiles ─────────────────────────────────────────────────────────────────
    /** Clay rock cluster, Varrock East mine (adjust ±3 tiles in-game if needed). */
    private static final Tile MINE_TILE = new Tile(3286, 3370, 0);
    private static final Area MINE_AREA = new Area(3278, 3362, 3296, 3378, 0);
    private static final Tile BANK_TILE = new Tile(3253, 3420, 0);
    private static final Area BANK_AREA = new Area(3249, 3416, 3257, 3424, 0);

    // ── Item IDs ──────────────────────────────────────────────────────────────
    private static final int ITEM_CLAY          = 434;
    private static final int ITEM_SOFT_CLAY     = 1761;
    private static final int ITEM_BUCKET        = 1925;
    private static final int ITEM_BUCKET_WATER  = 1929;

    // ── Batch settings ────────────────────────────────────────────────────────
    /**
     * Sell when this many soft clay have been banked.
     * ~200 per sell run to keep GE slots from piling up.
     */
    private static final int SELL_BATCH = 200;

    /** Per trip: mine this many clay + use this many buckets of water. */
    private static final int PER_TRIP   = 14;

    private int bankedSoftClay = 0;
    private State state = State.BANKING;   // start at bank to get water buckets

    private enum State { BANKING, MINING, SOFTENING, SELLING }

    // ─────────────────────────────────────────────────────────────────────────

    @Override public String id()          { return "soft_clay"; }
    @Override public String description() { return "Mine Clay → Soft Clay (Varrock East) → GE"; }
    @Override public int estimatedGpHr()  { return 60_000; }

    @Override public Map<Skill, Integer> requirements() {
        return Collections.emptyMap(); // truly zero requirements
    }

    @Override public int tick() {
        switch (state) {
            case BANKING:   return doBank();
            case MINING:    return doMine();
            case SOFTENING: return doSoften();
            case SELLING:   return doSell();
            default:        return 600;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BANK — withdraw PER_TRIP buckets of water, deposit soft clay
    // ─────────────────────────────────────────────────────────────────────────

    private int doBank() {
        if (!BANK_AREA.contains(Players.getLocal())) {
            Walking.walk(BANK_TILE);
            return Calculations.random(1400, 2200);
        }
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3000L);
        }
        if (!Bank.isOpen()) return Calculations.random(600, 1000);

        // Deposit any soft clay we already have
        if (Inventory.contains("Soft clay")) {
            int depositing = Inventory.count("Soft clay");
            Bank.depositAll("Soft clay");
            Sleep.sleepUntil(() -> !Inventory.contains("Soft clay"), 2000L);
            bankedSoftClay += depositing;
        }
        // Deposit any raw clay too (unused from last trip)
        if (Inventory.contains("Clay")) {
            Bank.depositAll("Clay");
            Sleep.sleepUntil(() -> !Inventory.contains("Clay"), 2000L);
        }

        // Make sure we have full buckets of water in bank; if only buckets, fill them
        // (For simplicity we withdraw pre-filled buckets of water from bank stock)
        if (!Bank.contains("Bucket of water")) {
            // Try filling empty buckets from a water source — for now just stop if none available
            // A human would have pre-filled some; the route will pause gracefully
            Bank.close();
            return Calculations.random(3000, 5000); // wait and retry next tick
        }

        // Withdraw exactly PER_TRIP buckets of water
        Bank.withdraw("Bucket of water", PER_TRIP);
        Sleep.sleepUntil(() -> Inventory.count("Bucket of water") >= PER_TRIP, 2000L);

        Bank.close();

        // Decide next state
        if (bankedSoftClay >= SELL_BATCH) {
            state = State.SELLING;
        } else {
            state = State.MINING;
        }
        return Calculations.random(400, 700);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MINE — fill remaining inventory slots with clay
    // ─────────────────────────────────────────────────────────────────────────

    private int doMine() {
        int clayCurrent = Inventory.count("Clay");
        int bucketsLeft = Inventory.count("Bucket of water");

        // Done mining when clay count equals available buckets (ready to soften)
        if (clayCurrent >= bucketsLeft && bucketsLeft > 0) {
            state = State.SOFTENING;
            return 300;
        }
        // Or if inventory is full
        if (Inventory.isFull()) {
            state = State.SOFTENING;
            return 300;
        }

        if (!MINE_AREA.contains(Players.getLocal())) {
            Walking.walk(MINE_TILE);
            return Calculations.random(1400, 2200);
        }

        // Find a clay rock and mine it
        GameObject clayRock = GameObjects.closest(g -> g != null
                && MINE_AREA.contains(g.getTile())
                && (g.getName().equalsIgnoreCase("Rocks") || g.getName().equalsIgnoreCase("Clay rocks"))
                && g.hasAction("Mine"));

        if (clayRock == null) {
            // Rocks depleted — wait for respawn (clay respawns ~1.6s per rock)
            return Calculations.random(1600, 3200);
        }

        if (!Players.getLocal().isAnimating()) {
            clayRock.interact("Mine");
            Sleep.sleepUntil(() -> Players.getLocal().isAnimating()
                    || Inventory.count("Clay") > clayCurrent, 3000L);
        }
        return Calculations.random(1200, 2000);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SOFTEN — use each bucket of water on each clay piece
    // ─────────────────────────────────────────────────────────────────────────

    private int doSoften() {
        int softened  = Inventory.count("Soft clay");
        int clayCnt   = Inventory.count("Clay");
        int bucketCnt = Inventory.count("Bucket of water");

        // All clay softened → bank
        if (clayCnt == 0) {
            state = State.BANKING;
            return 300;
        }
        if (bucketCnt == 0) {
            // Used all buckets; bank what we have
            state = State.BANKING;
            return 300;
        }

        // "Use" bucket of water on clay — DreamBot item-on-item interaction
        Item bucket = Inventory.get("Bucket of water");
        Item clay   = Inventory.get("Clay");
        if (bucket == null || clay == null) {
            state = State.BANKING;
            return 300;
        }

        bucket.interact("Use");
        if (Inventory.isItemSelected()) {
            clay.interact("Use");
            // Wait for soft clay count to increase
            int expected = softened + 1;
            Sleep.sleepUntil(() -> Inventory.count("Soft clay") >= expected
                    || Inventory.count("Clay") < clayCnt, 2500L);
        }
        return Calculations.random(800, 1200);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SELL — queue batch for GESellTask
    // ─────────────────────────────────────────────────────────────────────────

    private int doSell() {
        // Look up current market price before queuing
        int sellPrice = PriceCache.getQuickSellPrice(ITEM_SOFT_CLAY);
        GESellTask.queue("Soft clay", bankedSoftClay, sellPrice);
        bankedSoftClay = 0;
        state = State.BANKING;
        return 300;
    }
}
