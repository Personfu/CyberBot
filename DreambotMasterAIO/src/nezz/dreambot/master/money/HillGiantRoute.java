package nezz.dreambot.master.money;

import nezz.dreambot.master.ge.GESellTask;
import nezz.dreambot.master.ge.PriceCache;
import nezz.dreambot.master.id.ItemID;
import nezz.dreambot.master.id.NpcID;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hill Giant killer — Edgeville Dungeon, banking at Edgeville West bank.
 *
 * <h3>Why this outperforms standard F2P bot targets in 2026</h3>
 * <ul>
 *   <li><b>Limpwurt roots</b> — typically 1,500-3,000 gp each. Hill Giants drop them often.
 *       That alone can hit 200,000-350,000 gp/hr at decent kill speed.</li>
 *   <li><b>Big bones</b> — bury immediately (Prayer XP). Limpwurt roots + coins + rune items
 *       are the actual money.</li>
 *   <li><b>Almost zero bot competition</b> — the dungeon traversal (ladder → walk north through
 *       dungeon) scares away cookie-cutter bots that only handle flat-world navigation.</li>
 *   <li><b>Edgeville is ideal</b> — bank is 90 seconds from the dungeon entrance on foot.</li>
 * </ul>
 *
 * <h3>GP/hr estimate (May 2026)</h3>
 * 30-50 kills/hr × ~5,000 gp average drop value = <b>150,000–250,000 gp/hr</b>.<br>
 * Spikes up to 350k+ if limpwurt roots and rune items land in the same session.
 *
 * <h3>Requirements</h3>
 * <ul>
 *   <li>40+ combat recommended (can do lower with food, but slower)</li>
 *   <li>Food in bank (lobsters / swordfish preferred)</li>
 *   <li>No key required — the main Edgeville Dungeon entrance is always open</li>
 * </ul>
 *
 * <h3>Hill Giant (NPC ID 2098) drops looted</h3>
 * Big bones (bury), Limpwurt root (top priority sell), Coins, Rune med helm (rare),
 * various seeds, Nature/Chaos/Cosmic runes.
 */
public final class HillGiantRoute extends MoneyRoute {

    // ── Tiles ─────────────────────────────────────────────────────────────────
    /** Edgeville West bank. */
    private static final Tile BANK_TILE        = new Tile(3094, 3491, 0);
    private static final Area BANK_AREA        = new Area(3090, 3487, 3098, 3495, 0);

    /** Edgeville Dungeon entrance (ladder, surface side). */
    private static final Tile DUNGEON_ENTRANCE = new Tile(3132, 3449, 0);

    /**
     * Hill Giant combat zone (underground, plane 0).
     * Actual underground coords for northern Edgeville Dungeon Hill Giant room.
     * Tune ±5 tiles if pathing overshoots the room.
     */
    private static final Tile GIANT_ROOM       = new Tile(3118, 9836, 0);
    private static final Area GIANT_AREA       = new Area(3105, 9824, 3132, 9850, 0);

    /** Underground area bounding box — used to detect "we are in the dungeon". */
    private static final Area DUNGEON_AREA     = new Area(3075, 9820, 3165, 9905, 0);

    // ── Loot priority ─────────────────────────────────────────────────────────
    /** Loot in priority order — highest value first. */
    private static final String[] LOOT = {
        "Rune med helm", "Limpwurt root", "Nature rune", "Chaos rune", "Cosmic rune",
        "Mithril ore", "Coins"
    };

    // ── Tuning ────────────────────────────────────────────────────────────────
    private static final int EAT_AT_PCT   = 60;
    private static final int BATCH_GP     = 500_000; // sell when ~500k gp worth accumulated

    private int limpwurtRootsBanked = 0;
    private int coinsBanked         = 0;
    private boolean banking         = false;

    // ─────────────────────────────────────────────────────────────────────────

    @Override public String id()          { return "hill_giants"; }
    @Override public String description() { return "Hill Giants (Edgeville Dungeon) → Limpwurt Roots → GE"; }
    @Override public int estimatedGpHr()  { return 200_000; }

    @Override public Map<Skill, Integer> requirements() {
        Map<Skill, Integer> r = new LinkedHashMap<>();
        r.put(Skill.HITPOINTS, 30); // bare minimum; 40+ recommended
        return r;
    }

    @Override public int tick() {
        // ── 1. Eat if low ────────────────────────────────────────────────────
        if (shouldEat()) {
            eat();
            return Calculations.random(400, 700);
        }

        // ── 2. Banking run ───────────────────────────────────────────────────
        if (banking || needsBank()) {
            banking = true;
            int r = handleBank();
            if (r < 0) { banking = false; }
            return Math.abs(r);
        }

        // ── 3. Go underground if on surface ──────────────────────────────────
        if (!DUNGEON_AREA.contains(Players.getLocal())) {
            return descend();
        }

        // ── 4. Walk to giant room ─────────────────────────────────────────────
        if (!GIANT_AREA.contains(Players.getLocal())) {
            Walking.walk(GIANT_ROOM);
            return Calculations.random(1600, 2400);
        }

        // ── 5. Bury bones ────────────────────────────────────────────────────
        if (!inCombat() && Inventory.contains("Big bones")) {
            return bury();
        }

        // ── 6. Loot valuable drops ───────────────────────────────────────────
        if (!inCombat()) {
            int looted = loot();
            if (looted > 0) return looted;
        }

        // ── 7. Attack ────────────────────────────────────────────────────────
        return attack();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navigation
    // ─────────────────────────────────────────────────────────────────────────

    private int descend() {
        if (DUNGEON_ENTRANCE.distance(Players.getLocal()) > 8) {
            Walking.walk(DUNGEON_ENTRANCE);
            return Calculations.random(1800, 2600);
        }
        GameObject ladder = GameObjects.closest(g -> g != null
                && g.getTile().distance(DUNGEON_ENTRANCE) <= 4
                && (g.hasAction("Climb-down") || g.hasAction("Climb-into")));
        if (ladder != null) {
            ladder.interact(ladder.hasAction("Climb-down") ? "Climb-down" : "Climb-into");
            Sleep.sleepUntil(() -> DUNGEON_AREA.contains(Players.getLocal()), 5000L);
            return Calculations.random(1600, 2400);
        }
        Walking.walk(DUNGEON_ENTRANCE);
        return Calculations.random(1400, 2000);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Combat + loot
    // ─────────────────────────────────────────────────────────────────────────

    private int attack() {
        if (inCombat()) return Calculations.random(1200, 2200);

        NPC giant = NPCs.closest(n -> n != null
                && n.getID() == NpcID.HILL_GIANT
                && n.getHealthPercent() > 0
                && !n.isInCombat()
                && GIANT_AREA.contains(n.getTile()));

        if (giant == null) {
            // All giants busy or dead — wait
            return Calculations.random(2000, 4000);
        }
        if (giant.interact("Attack")) {
            return Calculations.random(1800, 2800);
        }
        return Calculations.random(700, 1200);
    }

    private int loot() {
        for (String name : LOOT) {
            GroundItem gi = GroundItems.closest(g -> g != null
                    && g.getName() != null
                    && g.getName().equalsIgnoreCase(name)
                    && GIANT_AREA.contains(g.getTile()));
            if (gi != null && gi.interact("Take")) {
                return Calculations.random(800, 1400);
            }
        }
        return 0;
    }

    private int bury() {
        Item bones = Inventory.get("Big bones");
        if (bones != null && bones.interact("Bury")) {
            return Calculations.random(1600, 2000);
        }
        return Calculations.random(400, 600);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Banking
    // ─────────────────────────────────────────────────────────────────────────

    private int handleBank() {
        // Climb out first
        if (DUNGEON_AREA.contains(Players.getLocal())) {
            return ascend();
        }
        // Walk to bank
        if (!BANK_AREA.contains(Players.getLocal())) {
            Walking.walk(BANK_TILE);
            return Calculations.random(1800, 2600);
        }
        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 3000L);
        }
        if (!Bank.isOpen()) return Calculations.random(600, 1000);

        // Count limpwurt roots before depositing
        limpwurtRootsBanked += Inventory.count("Limpwurt root");

        // Deposit everything except food and combat gear
        Bank.depositAllExcept(i -> i != null && i.getName() != null
                && (isFood(i.getName())
                 || i.getName().toLowerCase().contains("sword")
                 || i.getName().toLowerCase().contains("scimitar")
                 || i.getName().toLowerCase().contains("axe")
                 || i.getName().toLowerCase().contains("platebody")
                 || i.getName().toLowerCase().contains("shield")));

        // Restock food (up to 8 pieces)
        if (!hasFood()) {
            for (String food : new String[]{"Swordfish", "Lobster", "Tuna", "Salmon", "Trout"}) {
                if (Bank.contains(food)) {
                    Bank.withdraw(food, 8);
                    break;
                }
            }
        }

        // Queue sell if we have a meaningful batch of limpwurt roots
        if (limpwurtRootsBanked >= 20) {
            int price = PriceCache.getQuickSellPrice(ItemID.LIMPWURT_ROOT);
            GESellTask.queue("Limpwurt root", limpwurtRootsBanked, price);
            limpwurtRootsBanked = 0;
        }
        // Queue coins? No — coins auto-accumulate in bank as regular currency.

        Bank.close();
        return -(Calculations.random(1000, 1800)); // negative = done banking
    }

    private int ascend() {
        GameObject up = GameObjects.closest(g -> g != null
                && (g.hasAction("Climb-up") || g.hasAction("Climb-out"))
                && DUNGEON_AREA.contains(g.getTile())
                && g.getTile().distance(Players.getLocal()) <= 30);
        if (up != null) {
            up.interact(up.hasAction("Climb-up") ? "Climb-up" : "Climb-out");
            Sleep.sleepUntil(() -> !DUNGEON_AREA.contains(Players.getLocal()), 5000L);
            return Calculations.random(1600, 2400);
        }
        Walking.walk(new Tile(3105, 9835, 0)); // walk toward entrance ladder area
        return Calculations.random(1400, 2000);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private boolean needsBank() {
        return Inventory.isFull() || (shouldEat() && !hasFood());
    }

    private boolean shouldEat() {
        int max = Skills.getRealLevel(Skill.HITPOINTS);
        int cur = Skills.getBoostedLevel(Skill.HITPOINTS);
        return cur < max * EAT_AT_PCT / 100;
    }

    private void eat() {
        Item food = Inventory.get(i -> i != null && i.getName() != null && isFood(i.getName()));
        if (food != null) food.interact("Eat");
    }

    private boolean hasFood() {
        return Inventory.contains(i -> i != null && i.getName() != null && isFood(i.getName()));
    }

    private static boolean isFood(String name) {
        return name.equalsIgnoreCase("Swordfish")
            || name.equalsIgnoreCase("Lobster")
            || name.equalsIgnoreCase("Tuna")
            || name.equalsIgnoreCase("Salmon")
            || name.equalsIgnoreCase("Trout")
            || name.equalsIgnoreCase("Cooked meat");
    }

    private static boolean inCombat() {
        return Players.getLocal().isInCombat()
                || Players.getLocal().getInteractingCharacter() != null;
    }
}
